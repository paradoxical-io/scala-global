package io.paradoxical.common.execution

import scala.collection.parallel.{ExecutionContextTaskSupport, ParIterableLike, TaskSupport}
import scala.concurrent.ExecutionContext

class ParallelizableExtensions[Y <: ParIterableLike[_, _, _]](
  par: Y
) {
  def withTaskSupport(taskSupport: TaskSupport): Y = {
    par.tasksupport = taskSupport
    par
  }

  /**
   * Run the parallel computation on a provided execution context
   *
   * This should be one that has proper thread local tooled through
   *
   * For more information on how to get an execution context please read
   *
   *
   * @param executionContext
   * @return
   */
  def safe(implicit executionContext: ExecutionContext): Y = {
    withTaskSupport(new ExecutionContextTaskSupport(executionContext))
  }
}
