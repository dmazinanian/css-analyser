
package css.intersection;

import ca.concordia.cssanalyser.cssmodel.StyleSheet;
import ca.concordia.cssanalyser.cssmodel.declaration.Declaration;
import ca.concordia.cssanalyser.cssmodel.selectors.Selector;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSInterSelectorValueOverridingDependency;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSInterSelectorValueOverridingDependency.InterSelectorDependencyReason;
import ca.concordia.cssanalyser.refactoring.dependencies.CSSValueOverridingDependencyList;

public class CSSDomFreeDependencyDetector {

    public static CSSValueOverridingDependencyList findOverridingDependencies(StyleSheet styleSheet) {
		CSSValueOverridingDependencyList dependencies = new CSSValueOverridingDependencyList();
        CSSInterSelectorValueOverridingDependency d;

        for (Selector s1 : styleSheet.getAllSelectors()) {
            System.out.println("selector: " + s1);
            for (Declaration d1 : s1.getDeclarations()) {
                for (Selector s2 : styleSheet.getAllSelectors()) {
                    for (Declaration d2 : s2.getDeclarations()) {
                        if (d1.getProperty().equals(d2.getProperty()) &&
                            styleSheet.getSelectorNumber(s1) < styleSheet.getSelectorNumber(s2)) {
                                d = new CSSInterSelectorValueOverridingDependency(s1, d1, s2, d2,
                                                                                  d1.getProperty(),
															                      InterSelectorDependencyReason.DUE_TO_CASCADING);
                                System.out.println("dep: " + d);
                                dependencies.add(d);
                        }
                    }
                }
            }
        }

        return dependencies;
    }


}
