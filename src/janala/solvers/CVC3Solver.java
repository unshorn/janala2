/*
 * Copyright (c) 2012, NTT Multimedia Communications Laboratories, Inc. and Koushik Sen
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * Author: Koushik Sen (ksen@cs.berkeley.edu)
 */

package janala.solvers;

import gnu.trove.iterator.TIntLongIterator;
import janala.config.Config;
import janala.interpreters.*;
import janala.utils.MyLogger;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Author: Koushik Sen (ksen@cs.berkeley.edu)
 * Date: 7/12/12
 * Time: 10:03 AM
 */
public class CVC3Solver implements Solver {
    ArrayList<Value> inputs;
    ArrayList<Constraint> constraints;
    int pathConstraintIndex;
    private final static Logger logger = MyLogger.getLogger(CVC3Solver.class.getName());
    private final static Logger tester = MyLogger.getTestLogger(Config.mainClass+"."+Config.iteration);

    public void setInputs(ArrayList<Value> inputs) {
        this.inputs = inputs;
    }

    public void setPathConstraint(ArrayList<Constraint> pathConstraint) {
        this.constraints = pathConstraint;
    }

    public void setPathConstraintIndex(int pathConstraintIndex) {
        this.pathConstraintIndex = pathConstraintIndex;
    }

    public void visitSymbolicInt(SymbolicInt c) {
    }

    public void visitSymbolicIntCompare(SymbolicIntCompareConstraint c) {
    }

    public void visitSymbolicOr(SymbolicOrConstraint c) {
    }

    public void visitSymbolicAnd(SymbolicAndConstraint c) {
    }

    public void visitSymbolicNot(SymbolicNotConstraint c) {
    }

    public void visitSymbolicTrue(SymbolicTrueConstraint c) {
    }

    public void visitSymbolicFalse(SymbolicFalseConstraint c) {
    }

    public void visitSymbolicStringPredicate(SymbolicStringPredicate c) {
    }

    private void print(Constraint con, PrintStream out, LinkedHashSet<String> freeVars, CONSTRAINT_TYPE type, TreeMap<String,Long> soln) {
        if (con instanceof SymbolicInt) {
            SymbolicInt c = (SymbolicInt)con;
            boolean first2 = true;
            for ( TIntLongIterator it = c.linear.iterator(); it.hasNext(); ) {
                it.advance();

                int integer = it.key();
                freeVars.add("x"+integer);
                long l = it.value();
                if (first2) {
                    first2 = false;
                } else {
                    out.print(" + ");
                }
                out.print('x');
                out.print(integer);
                out.print("*(");
                out.print(l);
                out.print(')');

            }
            if (c.constant != 0) {
                out.print("+(");
                out.print(c.constant);
                out.print(')');
            }
            if (c.op == SymbolicInt.COMPARISON_OPS.EQ) {
                out.print(" = ");
            } else if (c.op == SymbolicInt.COMPARISON_OPS.NE) {
                out.print(" /= ");
            } else if (c.op == SymbolicInt.COMPARISON_OPS.LE) {
                out.print(" <= ");
            } else if (c.op == SymbolicInt.COMPARISON_OPS.LT) {
                out.print(" < ");
            } else if (c.op == SymbolicInt.COMPARISON_OPS.GE) {
                out.print(" >= ");
            } else if (c.op == SymbolicInt.COMPARISON_OPS.GT) {
                out.print(" > ");
            }
            out.print("0");
        } else if (con instanceof SymbolicIntCompareConstraint) {
            SymbolicIntCompareConstraint c = (SymbolicIntCompareConstraint)con;
            out.print('(');
            if (c.left.sym!=null) freeVars.add(c.left.sym);
            out.print(c.left);
            out.print(')');
            out.print('-');
            out.print('(');
            if (c.right.sym!=null) freeVars.add(c.right.sym);
            out.print(c.right);
            out.print(')');
            if (c.op == SymbolicIntCompareConstraint.COMPARISON_OPS.EQ) {
                out.print(" = ");
            } else if (c.op == SymbolicIntCompareConstraint.COMPARISON_OPS.NE) {
                out.print(" /= ");
            } else if (c.op == SymbolicIntCompareConstraint.COMPARISON_OPS.LE) {
                out.print(" <= ");
            } else if (c.op == SymbolicIntCompareConstraint.COMPARISON_OPS.LT) {
                out.print(" < ");
            } else if (c.op == SymbolicIntCompareConstraint.COMPARISON_OPS.GE) {
                out.print(" >= ");
            } else if (c.op == SymbolicIntCompareConstraint.COMPARISON_OPS.GT) {
                out.print(" > ");
            }
            out.print("0");
        } else if (con instanceof SymbolicOrConstraint) {
            SymbolicOrConstraint or = (SymbolicOrConstraint)con;

            boolean first2 = true;
            for(Constraint c:or.constraints) {
                if (first2) {
                    first2 = false;
                } else {
                    out.print(" OR ");
                }
                out.print("(");
                print(c,out, freeVars, type, soln);
                out.print(")");
            }
            if (or.constraints.isEmpty()) {
                out.print(" TRUE ");
            }
        } else if (con instanceof SymbolicAndConstraint) {
            SymbolicAndConstraint and = (SymbolicAndConstraint)con;

            boolean first2 = true;
            for(Constraint c:and.constraints) {
                if (first2) {
                    first2 = false;
                } else {
                    out.print(" AND ");
                }
                out.print("(");
                print(c,out, freeVars, type, soln);
                out.print(")");
            }
            if (and.constraints.isEmpty()) {
                out.print(" FALSE ");
            }
        } else if (con instanceof SymbolicNotConstraint) {
            SymbolicNotConstraint not = (SymbolicNotConstraint)con;
            out.print(" NOT ");
            out.print("(");
            print(not.constraint,out, freeVars, type, soln);
            out.print(")");
        } else if (con instanceof SymbolicTrueConstraint) {
            out.print(" TRUE ");
        } else if (con instanceof SymbolicFalseConstraint) {
            out.print(" FALSE ");
        } else if (con instanceof SymbolicStringPredicate) {
            SymbolicStringPredicate str = (SymbolicStringPredicate) con;
            Constraint intConstraint = str.getFormula(freeVars, type, soln);
            print(con, out, freeVars, type, soln);
        } else {
            throw new RuntimeException("Unimplemented constraint type "+con);
        }
    }

    private void concatFile(LinkedHashSet<String> freeVars, String from, String to) throws java.io.IOException {
        PrintStream pw = new PrintStream(new BufferedOutputStream(new FileOutputStream(to)));

        for(String var:freeVars) {
            pw.print(var);
            pw.println(" :INT;");
        }

        BufferedReader br = new BufferedReader(new FileReader(from));
        String line = br.readLine();
        while (line != null) {
            pw.println(line);
            line = br.readLine();
        }
        br.close();
        pw.close();
    }

    private boolean writeFormula(String extra, CONSTRAINT_TYPE type, TreeMap<String,Long> soln) {
        try {
            boolean allTrue = true;
            Constraint tmp;
            PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(Config.instance.formulaFile+".tmp")));
//            int nInputs = inputs.size();
//            for (int i = 0; i < nInputs; i++) {
//                out.print("x");
//                out.print(i+1);
//                out.println(" : INT;");
//            }
//
            LinkedHashSet<String> freeVars = new LinkedHashSet<String>();
            for (int i=0; i<pathConstraintIndex;i++) {
                out.print("ASSERT ");
                tmp = constraints.get(i).substitute(soln);
                if (tmp != SymbolicTrueConstraint.instance) {
                    allTrue = false;
                }
                print(tmp, out, freeVars, type, soln);
                out.println(";");
            }
            if (extra != null) {
                out.print("ASSERT ");
                out.print(extra);
                out.println(";");
            }

            out.print("CHECKSAT ");
            tmp = constraints.get(pathConstraintIndex).not().substitute(soln);
            if (tmp != SymbolicTrueConstraint.instance) {
                allTrue = false;
            }
            print(tmp,out, freeVars, type, soln);
            out.println(";");
            out.println("COUNTERMODEL;");
            out.close();

            concatFile(freeVars, Config.instance.formulaFile+".tmp", Config.instance.formulaFile);
            return allTrue;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            logger.log(Level.SEVERE,"{0}",ioe);
            Runtime.getRuntime().halt(1);
            return false;
        }
    }

    private void writeInputs(TreeMap<String, Long> soln) {
        try {
            PrintStream out = new PrintStream(
                    new BufferedOutputStream(
                            new FileOutputStream(Config.instance.inputs)));
            int len = inputs.size();
            for(int i=0; i<len; i++) {
                Long l = soln.get("x"+(i+1));
                Value input = inputs.get(i);
                if (l!=null) {
                    if (input instanceof janala.interpreters.StringValue) {
                        //tester.log(Level.INFO, StringConstants.instance.get((int)(long)l));
                        out.println(StringConstants.instance.get((int)(long)l));
                    } else {
                        //tester.log(Level.INFO,l+"");
                        out.println(l);
                    }
                } else {
                    //tester.log(Level.INFO,input.getConcrete().toString());
                    out.println(input.getConcrete());
                }
            }
            out.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            logger.log(Level.SEVERE,"{0}",ioe);
            Runtime.getRuntime().halt(1);
        }
    }

    private String processInputs(BufferedReader br, TreeMap<String, Long> soln) {
        String line = null;
        String negatedSolution = null;

        try {
            line = br.readLine();
            if (!line.startsWith("Satisfiable")) {
                if (!line.contains("Unsat")) {
                    logger.log(Level.SEVERE,line);
                    logger.log(Level.SEVERE, "Call to CVC3 failed (concolic.cvc3 = "
                            + Config.instance.cvc3Command + ")");
                    Runtime.getRuntime().halt(1);
                }
                logger.log(Level.INFO,"-- Infeasible");
                while ((line = br.readLine()) != null) { }
                br.close();
                return null;
            } else {
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("ASSERT")) {
                        if (negatedSolution != null) {
                            negatedSolution += " AND "+line.substring(line.indexOf("("),line.indexOf(")")+1);
                        } else {
                            negatedSolution = line.substring(line.indexOf("("),line.indexOf(")")+1);
                        }

                        String[] tokens = line.split(" ");
//                        int xi = Integer.parseInt(tokens[1].substring(2));
                        String tmp = tokens[3].trim();
                        long val = Long.parseLong(tmp.substring(0, tmp.indexOf(")")));
                        soln.put(tokens[1].substring(1), val);
                    }
                }
                br.close();

                negatedSolution = "(NOT ("+negatedSolution+"))";
                return negatedSolution;
            }
        }  catch (IOException ioe) {
            ioe.printStackTrace();
            logger.log(Level.SEVERE,"{0}",ioe);
            Runtime.getRuntime().halt(1);
            return null;
        }
    }

    public boolean solve() {
        int count = 0, MAX_COUNT = 100;
        String extra = null, negatedSolution, negatedSolution2;
        //console.log("Doing search at "+i+ " with tail "+(tail+1));
        while(count < MAX_COUNT) {
            TreeMap<String, Long> soln = new TreeMap<String, Long>();
            negatedSolution = solve(extra, CONSTRAINT_TYPE.INT, soln);
            if (negatedSolution != null) {
                negatedSolution2 = solve(null, CONSTRAINT_TYPE.STR, soln);
                if (negatedSolution2 != null) {
                    writeInputs(soln);
                    return true;
                } else {
                    if (extra != null) {
                        extra = extra + " AND " + negatedSolution;
                    } else {
                        extra = negatedSolution;
                    }
                }
            } else {
                return false;
            }
            count++;
        }
        return false;
    }

    public enum CONSTRAINT_TYPE {INT, STR};

    public String solve(String extra, CONSTRAINT_TYPE type, TreeMap<String,Long> soln) {
        try {
            String negatedSolution;
            if (writeFormula(extra, type, soln)) {
                return "";
            }

            ProcessBuilder builder = new ProcessBuilder(new String[]{Config.instance.cvc3Command,Config.instance.formulaFile});
            builder.redirectErrorStream(true);
            Process process = builder.start();

            (new StreamGobbler(process.getErrorStream(),"ERROR")).start();

            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            negatedSolution = processInputs(br, soln);
            process.waitFor();
            tester.log(Level.INFO,"Feasible = "+(negatedSolution != null) +" at "+pathConstraintIndex);
            return negatedSolution;

        } catch (IOException ioe) {
            ioe.printStackTrace();
            logger.log(Level.SEVERE,"{0}",ioe);
            Runtime.getRuntime().halt(1);
            return null;
        } catch (InterruptedException ie) {
            ie.printStackTrace();
            logger.log(Level.SEVERE,"{0}",ie);
            Runtime.getRuntime().halt(1);
            return null;
        }
    }
}

