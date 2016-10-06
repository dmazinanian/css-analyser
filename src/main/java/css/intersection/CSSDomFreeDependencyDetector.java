
package css.intersection;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import ca.concordia.cssanalyser.app.FileLogger;
import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.BaseSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.GroupingSelector;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSInterSelectorValueOverridingDependency;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSInterSelectorValueOverridingDependency.InterSelectorDependencyReason;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependencyList;

public class CSSDomFreeDependencyDetector {

    private static Logger LOGGER
        = FileLogger.getLogger(CSSDomFreeDependencyDetector.class);

    private static class SelDec {
        public BaseSelector selector;
        public Declaration declaration;
        public int selector_number;
        public int declaration_number;

        public SelDec(BaseSelector selector,
                      Declaration declaration,
                      int selector_number,
                      int declaration_number) {
            this.selector = selector;
            this.declaration = declaration;
            this.selector_number = selector_number;
            this.declaration_number = declaration_number;
        }

        /**
         * @param sd another SelDec
         * @return true iff this declaration preceeds sd in the styleSheet
         */
        public boolean preceeds(SelDec sd) {
            return ((this.selector_number < sd.selector_number) ||
                    ((this.selector_number == sd.selector_number) &&
                     (this.declaration_number < sd.declaration_number)));
        }

        public boolean equals(Object o) {
            if (o instanceof SelDec) {
                SelDec sd = (SelDec)o;
                return this.selector_number == sd.selector_number &&
                       this.declaration_number == sd.declaration_number &&
                       this.selector.equals(sd.selector) &&
                       this.declaration.equals(sd.declaration);
            }
            return false;
        }
    }

    private static class PropSpec {
        public String property;
        public int specificity;

        public PropSpec(String property, int specifity) {
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
        try {
            emptinessChecker =
                new ProcessBuilder().command("pypy", "/home/matt/research/css/satcss/implementation/main.py", "-e").start();

            OutputStream out = emptinessChecker.getOutputStream();
            empOut = new OutputStreamWriter(out);

            InputStream in = emptinessChecker.getInputStream();
            empIn = new InputStreamReader(in);
        } catch (IOException e) {
            LOGGER.error("Failed to start python emptiness checker, will assume all selectors can overlap." + e);
            emptinessChecker = null;
        }
    }
    /**
     * @return the orderings within the file that must be respected.  I.e. there
     * exists a DOM where reordering the dependencies would cause a different
     * rendering
     */
    public CSSValueOverridingDependencyList findOverridingDependencies() {
        buildOverlapMap();
        return buildDependencyList();
    }

    /**
     * After building overlapMap (buildOverlapMap()) call this function to build
     * the dependency list
     *
     * @return the dependency list
     */
    private CSSValueOverridingDependencyList buildDependencyList() {
		CSSValueOverridingDependencyList dependencies = new CSSValueOverridingDependencyList();
        CSSInterSelectorValueOverridingDependency dep;


        for (Map.Entry<PropSpec, Set<SelDec>> e : overlapMap.entrySet()) {
            String property = e.getKey().property;
            Set<SelDec> sds = e.getValue();

            for (SelDec sd1 : sds) {
                for (SelDec sd2 : sds) {
                    if (!sd1.equals(sd2) &&
                        !sd1.declaration.equals(sd2.declaration) &&
                        selectorsOverlap(sd1.selector,
                                         sd2.selector)) {

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
                        dependencies.add(dep);
                    }
                }
            }
        }

        return dependencies;
    }

    private boolean selectorsOverlap(BaseSelector s1,
                                     BaseSelector s2) {
        if (emptinessChecker == null)
            return true;

        try {
            empOut.write(s1 + "\n");
            empOut.write(s2 + "\n");

            LOGGER.info("Written to python");

            int result = empIn.read();
            // read \n
            empIn.read();

            if (result == -1)
                throw new IOException("Unexpected end of python emptiness checker input stream.");
            LOGGER.info("Got " + result);

            return result != 'E';
        } catch (IOException e) {
            LOGGER.error("Error communicating with python emptiness checker, assuming all selectors overlap." + e);
            emptinessChecker = null;
            return true;
        }
    }


    /**
     * populates overlapMap with data from this.styleSheet
     */
    private void buildOverlapMap() {
        overlapMap.clear();

        for (Selector s : styleSheet.getAllSelectors()) {
            for (Declaration d : s.getDeclarations()) {
                int sn = styleSheet.getSelectorNumber(s);
                int dn = s.getDeclarationNumber(d);
                if (s instanceof BaseSelector) {
                    addNewRule((BaseSelector)s, d, sn, dn);
                } else if (s instanceof GroupingSelector) {
                    GroupingSelector g = (GroupingSelector)s;
                    for (BaseSelector bs : g) {
                        addNewRule(bs, d, sn, dn);
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
                            Declaration d,
                            int selector_number,
                            int declaration_number) {
        int specificity = s.getSpecificity();
        PropSpec ps = new PropSpec(d.getProperty(), specificity);
        Set<SelDec> sds = overlapMap.get(ps);
        if (sds == null) {
            sds = new HashSet<SelDec>();
            overlapMap.put(ps, sds);
        }
        sds.add(new SelDec(s, d, selector_number, declaration_number));
    }

}
