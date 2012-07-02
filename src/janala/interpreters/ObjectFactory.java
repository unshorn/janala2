/*
 * Author: Koushik Sen (ksen@cs.berkeley.edu)
 */

package janala.interpreters;

/**
 * Author: Koushik Sen (ksen@cs.berkeley.edu)
 * Date: 6/27/12
 * Time: 6:57 PM
 */
public class ObjectFactory {
    public static ObjectValue create(int nFields, String className) {
        if (className.equals("java/lang/Integer")) {
            return new IntegerObjectValue();
        }
        return new ObjectValue(nFields);
    }
}
