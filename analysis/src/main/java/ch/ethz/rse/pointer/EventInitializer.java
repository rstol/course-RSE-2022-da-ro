package ch.ethz.rse.pointer;

import java.util.LinkedList;
import java.util.List;

import soot.jimple.internal.JInvokeStmt;

/**
 *
 * Contains information about the initializer of a Event object
 *
 */
public class EventInitializer {

  /**
   * statement that performs the initialization
   */
  private final JInvokeStmt statement;

  /**
   * Unique identifier of the initializer
   */
  private final int uniqueNumber;

  /**
   * first argument in the constructor
   */
  public final int start;

  /**
   * the variable associated to this initializer
   */
  private final String var;

  /**
   * The invoke statements associated to this initializer
   */
  private List<JInvokeStmt> invokes = new LinkedList<JInvokeStmt>();

  /**
   *
   * @param statement    piece of code running the initializer
   * @param uniqueNumber unique identifier of the initializer
   * @param argment      argument in the constructor
   */
  public EventInitializer(JInvokeStmt statement, int uniqueNumber, int start, String var) {
    this.statement = statement;
    this.uniqueNumber = uniqueNumber;
    this.start = start;
    this.var = var;
  }

  /**
   *
   * @return piece of code running the initializer
   */
  public JInvokeStmt getStatement() {
    return statement;
  }

  /**
   *
   * @return unique identifier of the initializer
   */
  private int getUniqueNumber() {
    return this.uniqueNumber;
  }

  /**
   *
   * @return variable associated to this initializer
   */
  public String getVar() {
    return var;
  }

  /**
   *
   * @param s invoke statemet to add to this initializer
   */
  public void addInvoke(JInvokeStmt s) {
    invokes.add(s);
  }

  /**
   *
   * @return invoke statements associated to this initializer
   */
  public List<JInvokeStmt> getInvokes() {
    return invokes;
  }

  /**
   *
   * @return unique label of this initializer
   */
  public String getUniqueLabel() {
    return "AbstractObject" + this.getUniqueNumber() + ".end";
  }

  public String toString() {
    return "AbstractObject" + this.getUniqueNumber();
  }

}