/*
 * Author: Koushik Sen (ksen@cs.berkeley.edu)
 */

package janala.logger.inst;

public class GETVALUE_boolean extends Instruction {
    public boolean v;

    public GETVALUE_boolean(boolean v) {
        super(-1, -1);
        this.v = v;
    }

    public void visit(IVisitor visitor) {
	visitor.visitGETVALUE_boolean(this);
    }

    @Override
    public String toString() {
        return "GETVALUE_boolean v="+v;
    }
}
