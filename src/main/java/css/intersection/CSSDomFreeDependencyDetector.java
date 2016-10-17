
package css.intersection;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import ca.concordia.cssanalyser.app.FileLogger;
import ca.concordia.cssanalyser.cssmodel.LocationInfo;
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.BaseSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.GroupingSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSInterSelectorValueOverridingDependency;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSInterSelectorValueOverridingDependency.InterSelectorDependencyReason;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependencyList;

public class CSSDomFreeDependencyDetector {

    private static final String PYTHON_COMMAND = "./intersection-tool.sh";

    private static Logger LOGGER
        = FileLogger.getLogger(CSSDomFreeDependencyDetector.class);

    private static class SelDec {
        public BaseSelector selector;
        public Declaration declaration;

        public SelDec(BaseSelector selector,
                      Declaration declaration) {
            this.selector = selector;
            this.declaration = declaration;
        }

        /**
         * @param sd another SelDec
         * @return true iff this declaration preceeds sd in the styleSheet
         */
        public boolean preceeds(SelDec sd) {
            LocationInfo li1 = selector.getSelectorNameLocationInfo();
            LocationInfo li2 = sd.selector.getSelectorNameLocationInfo();

            int line1 = li1.getLineNumber();
            int line2 = li2.getLineNumber();

            return ((line1 < line2) ||
                    ((line1 == line2) &&
                     (li1.getColumnNumber() < li2.getColumnNumber())));
        }

        public boolean equals(Object o) {
            if (o instanceof SelDec) {
                SelDec sd = (SelDec)o;
                return // selector equals kind of makes sense here because
                       // location is important for dependency
                       this.selector.equals(sd.selector) &&
                       this.declaration.equals(sd.declaration);
            }
            return false;
        }
    }

    private static class PropSpec {
        public String property;
        public int specificity;

        public PropSpec(String property, int specificity) {
            this.property = property;
            this.specificity = specificity;
        }

        public boolean equals(Object o) {
            if (o instanceof PropSpec) {
                PropSpec ps = (PropSpec)o;
                return this.specificity == ps.specificity &&
                       this.property.equals(ps.property);
            }
            return false;
        }

        public int hashCode() {
            return property.hashCode() + specificity;
        }
    }

    /**
     * To overcome slowness in communicating with python, we're going to run a
     * number of different instances that take tasks from a BlockingQueue whose
     * end is marked by a "poison" task.
     */
    private static class DependencyTask {
        public SelDec sd1;
        public SelDec sd2;
        public String property;

        public DependencyTask(SelDec sd1,
                              SelDec sd2,
                              String property) {
            this.sd1 = sd1;
            this.sd2 = sd2;
            this.property = property;
        }
    }


    /**
     * Commutative pair of selector strings for hash key
     */
    private static class SelPair {
        public BaseSelector sel1;
        public BaseSelector sel2;

        public SelPair() { }

        public SelPair(BaseSelector sel1, BaseSelector sel2) {
            this.sel1 = sel1;
            this.sel2 = sel2;
        }

        public boolean equals(Object o) {
            if (o instanceof SelPair) {
                SelPair s = (SelPair)o;
                // use selectorEquals since it ignores location and we don't
                // care about those for overlapping
                return ((sel1.selectorEquals(s.sel1, false) &&
                         sel2.selectorEquals(s.sel2, false)) ||
                        (sel1.selectorEquals(s.sel2, false) &&
                         sel2.selectorEquals(s.sel1, false)));
            }
            return false;
        }

        public int hashCode() {
            return sel1.selectorHashCode(false) +
                   sel2.selectorHashCode(false);
        }
    }

    // Memoize calls to overlap checker for all instances
    private static Map<SelPair, Boolean> overlapMemo
        = new HashMap<SelPair, Boolean>();
    // Temp SelPair to avoid creating objects just to check membership
    private static SelPair tempSelPair = new SelPair();


    // (p, spec) -> { ... (s, d) ... }
    // collects rules with the same property and specificity
    private Map<PropSpec, Set<SelDec>> overlapMap
        = new HashMap<PropSpec, Set<SelDec>>();
    private StyleSheet styleSheet;

    private Process emptinessChecker;
    private OutputStreamWriter empOut;
    private InputStreamReader empIn;

    public CSSDomFreeDependencyDetector(StyleSheet styleSheet) {
        this.styleSheet = styleSheet;
    }


    /**
     * @return the orderings within the file that must be respected.  I.e. there
     * exists a DOM where reordering the dependencies would cause a different
     * rendering
     */
    public CSSValueOverridingDependencyList findOverridingDependencies() {
        try {
            buildOverlapMap();
            return buildDependencyList();
        } catch (IOException e) {
            LOGGER.error("IOException calculating dependencies, aborting.\n" + e);
            System.exit(-1);
            return null;
        }
    }

    private void startPython() throws IOException {
        File pythonCommand = new File(PYTHON_COMMAND);
        if (!pythonCommand.exists())
            throw new IOException("Please create " +
                                  PYTHON_COMMAND +
                                  " script to start emptiness checker tool (you probably want a script that runs \"python <path to our main.py> -e\"");

        emptinessChecker =
            new ProcessBuilder().command(PYTHON_COMMAND).start();

        OutputStream out = emptinessChecker.getOutputStream();
        empOut = new OutputStreamWriter(out);

        InputStream in = emptinessChecker.getInputStream();
        empIn = new InputStreamReader(in);
    }

    /**
     * After building overlapMap (buildOverlapMap()) call this function to build
     * the dependency list
     *
     * @return the dependency list
     */
    private CSSValueOverridingDependencyList buildDependencyList()
            throws IOException {
        startPython();

        LOGGER.info("Starting to find dependencies...");

        long startTime = System.currentTimeMillis();

		CSSValueOverridingDependencyList dependencies = new CSSValueOverridingDependencyList();

        List<DependencyTask> tasks = new LinkedList<DependencyTask>();

        // first post all comparisons to python
        for (Map.Entry<PropSpec, Set<SelDec>> e : overlapMap.entrySet()) {
            String property = e.getKey().property;
            Set<SelDec> sds = e.getValue();

            SelDec[] sdArray = sds.toArray(new SelDec[sds.size()]);
            int len = sdArray.length;

            for (int i = 0; i < len; ++i) {
                for (int j = i + 1; j < len; ++j) {
                    SelDec sd1 = sdArray[i];
                    SelDec sd2 = sdArray[j];
                    if (!sd1.equals(sd2) &&
                        !sd1.declaration.equals(sd2.declaration)) {
                        Boolean memoRes = getMemoResult(sd1.selector, sd2.selector);
                        if (memoRes == null) {
                            empOut.write(sd1.selector + "\n");
                            empOut.write(sd2.selector + "\n");
                            tasks.add(new DependencyTask(sd1, sd2, property));
                        } else if (memoRes.equals(Boolean.TRUE)) {
                            dependencies.add(makeDependency(sd1, sd2, property));
                        }
                    }
                }
            }
        }

        // close, forcing python to flush
        empOut.close();

        long midTime = System.currentTimeMillis();

        LOGGER.info("Number of dependencies tasks " +
                    tasks.size() +
                    " calculated in " +
                    (midTime - startTime) +
                    "ms.");

        // get results
        for (DependencyTask t : tasks) {
            int result = empIn.read();

            if ((char)result == 'N') {
                dependencies.add(makeDependency(t.sd1, t.sd2, t.property));
                setMemoResult(t.sd1.selector, t.sd2.selector, true);
            } else {
                setMemoResult(t.sd1.selector, t.sd2.selector, false);
            }
        }

        long endTime = System.currentTimeMillis();

        LOGGER.info("Calculating dependencies took " +
                    (endTime - startTime) +
                    "ms.");

        return dependencies;
    }


    /**
     * @param sel1
     * @param sel2
     * @return null if overlap test of pair not memoed, boolean for true if
     * there is an overlap, boolean of false if not
     */
    private Boolean getMemoResult(BaseSelector sel1,
                                  BaseSelector sel2) {
        tempSelPair.sel1 = sel1;
        tempSelPair.sel2 = sel2;
        return overlapMemo.get(tempSelPair);
    }

    /**
     * Memoizes overlap result
     *
     * @param sel1
     * @param sel2
     * @param overlap true if sel1 and sel2 can overlap
     */
    private void setMemoResult(BaseSelector sel1,
                               BaseSelector sel2,
                               boolean overlap) {
        SelPair sp = new SelPair(sel1, sel2);
        overlapMemo.put(sp, Boolean.valueOf(overlap));
    }

    /**
     * @param sd1
     * @param sd2
     * @param property the property that sd1 and sd2 pertain to
     * @return a new dependency object calculate by order of sd1 and sd2
     */
    private CSSInterSelectorValueOverridingDependency makeDependency(SelDec sd1,
                                                                     SelDec sd2,
                                                                     String property) {
        CSSInterSelectorValueOverridingDependency dep;
        if (sd1.preceeds(sd2)) {
            dep = new CSSInterSelectorValueOverridingDependency(
                            sd1.selector,
                            sd1.declaration,
                            sd2.selector,
                            sd2.declaration,
                            property,
                            InterSelectorDependencyReason.DUE_TO_CASCADING);
        } else {
            dep = new CSSInterSelectorValueOverridingDependency(
                            sd2.selector,
                            sd2.declaration,
                            sd1.selector,
                            sd1.declaration,
                            property,
                            InterSelectorDependencyReason.DUE_TO_CASCADING);
        }
        return dep;
    }


    /**
     * populates overlapMap with data from this.styleSheet
     */
    private void buildOverlapMap() {
        overlapMap.clear();

        for (Selector s : styleSheet.getAllSelectors()) {
            for (Declaration d : s.getDeclarations()) {
                if (s instanceof BaseSelector) {
                    addNewRule((BaseSelector)s, d);
                } else if (s instanceof GroupingSelector) {
                    GroupingSelector g = (GroupingSelector)s;
                    for (BaseSelector bs : g) {
                        addNewRule(bs, d);
                    }
                }
            }
        }
    }

    /**
     * Adds the rule (s, d) to the overlapMap.
     *
     * @param s a selector
     * @param d a declaration
     * @param selector_number the number of the selector containing s
     * @param declaration_number the number of the declaration
     */
    private void addNewRule(BaseSelector s,
                            Declaration d) {
        int specificity = s.getSpecificity();
        PropSpec ps = new PropSpec(d.getProperty(), specificity);
        Set<SelDec> sds = overlapMap.get(ps);
        if (sds == null) {
            sds = new HashSet<SelDec>();
            overlapMap.put(ps, sds);
        }
        sds.add(new SelDec(s, d));
    }

}
