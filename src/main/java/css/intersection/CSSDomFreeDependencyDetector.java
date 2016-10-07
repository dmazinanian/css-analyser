
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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

    private static final int NUM_WORKERS
        = Runtime.getRuntime().availableProcessors();

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

        public PropSpec(String property, int specificityArg) {
            this.property = property;
            this.specificity = specificityArg;
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

        /** Use only for "poison" task **/
        public DependencyTask() {
            this.sd1 = null;
        }

        public DependencyTask(SelDec sd1,
                              SelDec sd2,
                              String property) {
            this.sd1 = sd1;
            this.sd2 = sd2;
            this.property = property;
        }

        public boolean isPoison() {
            return sd1 == null;
        }
    }

    private final static DependencyTask POISON = new DependencyTask();

    /**
     * These are the workers who will communicate with python to complete tasks
     * posted to a queue by the main thread
     */
    private class DependencyWorkerThread extends Thread {
        private BlockingQueue<DependencyTask> queue;
        private CSSValueOverridingDependencyList dependencies;

        // The python process
        private Process emptinessChecker;
        private OutputStreamWriter empOut;
        private InputStreamReader empIn;

        /**
         * @param queue a blocking queue where tasks will be posted
         * @param dependencies the dependency list to add found dependencies to
         * (will synchronize on it to avoid concurrency errors)
         */
        public DependencyWorkerThread(BlockingQueue<DependencyTask> queue,
                                      CSSValueOverridingDependencyList dependencies) {
            this.queue = queue;
            this.dependencies = dependencies;

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

        public void run() {
            try {
                for (;;) {
                    DependencyTask task = queue.take();

                    if (task.isPoison())
                        break;

                    SelDec sd1 = task.sd1;
                    SelDec sd2 = task.sd2;
                    String property = task.property;

                    if (selectorsOverlap(sd1.selector,
                                         sd2.selector)) {
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

                        synchronized (dependencies) {
                            dependencies.add(dep);
                        }
                    }


                }
            } catch (InterruptedException e) {
                // do nothing but die
            }
        }

        private boolean selectorsOverlap(BaseSelector s1,
                                         BaseSelector s2) {
            if (emptinessChecker == null)
                return true;

            try {
                empOut.write(s1 + "\n");
                empOut.write(s2 + "\n");
                empOut.flush();

                int result = empIn.read();

                if (result == -1)
                    throw new IOException("Unexpected end of python emptiness checker input stream.");

                return result != 'E';
            } catch (IOException e) {
                LOGGER.error("Error communicating with python emptiness checker, assuming all selectors overlap." + e);
                emptinessChecker = null;
                return true;
            }
        }
    }

    // (p, spec) -> { ... (s, d) ... }
    // collects rules with the same property and specificity
    private Map<PropSpec, Set<SelDec>> overlapMap
        = new HashMap<PropSpec, Set<SelDec>>();
    private StyleSheet styleSheet;

    public CSSDomFreeDependencyDetector(StyleSheet styleSheet) {
        this.styleSheet = styleSheet;
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

        long startTime = System.currentTimeMillis();

		CSSValueOverridingDependencyList dependencies = new CSSValueOverridingDependencyList();

        BlockingQueue<DependencyTask> queue
            = new LinkedBlockingQueue<DependencyTask>();

        DependencyWorkerThread[] workers
            = new DependencyWorkerThread[NUM_WORKERS];

        for (int i = 0; i < NUM_WORKERS; ++i) {
            workers[i] = new DependencyWorkerThread(queue, dependencies);
            workers[i].start();
        }

        for (Map.Entry<PropSpec, Set<SelDec>> e : overlapMap.entrySet()) {
            String property = e.getKey().property;
            Set<SelDec> sds = e.getValue();

            for (SelDec sd1 : sds) {
                for (SelDec sd2 : sds) {
                    if (!sd1.equals(sd2) &&
                        !sd1.declaration.equals(sd2.declaration)) {
                        queue.add(new DependencyTask(sd1, sd2, property));
                    }
                }
            }
        }

        for (int i = 0; i < NUM_WORKERS; ++i)
            queue.add(POISON);
        for (int i = 0; i < NUM_WORKERS; ++i) {
            try {
                workers[i].join();
            } catch (InterruptedException e) {
                LOGGER.error("Interrupted joining with worker threads, dependencies may be wrong!" + e);
            }
        }

        long endTime = System.currentTimeMillis();

        LOGGER.info("Calculating dependencies took " +
                    (endTime - startTime) +
                    "ms.");

        return dependencies;
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
