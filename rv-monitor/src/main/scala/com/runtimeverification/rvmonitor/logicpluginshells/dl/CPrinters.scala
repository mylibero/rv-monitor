package com.runtimeverification.rvmonitor.logicpluginshells.dl
import scala.collection.JavaConverters._

object CPrinters {

  case class Param(pType: String, pName: String) {
    override def toString: String = s"""$pType $pName"""
  }

  case class CFunction(modifier: Option[String], returnType: String, name:String, params: Seq[Param], content: String) {

    val paramsList = params.mkString(", ")

    override def toString: String = {
      val template =
      s"""$returnType $name($paramsList)""" + "\n" +
      "{\t" + s"""$content""" + "\n}"

      modifier match {
        case None => template
        case Some(mod) => mod + " " + template
      }
    }
  }

  def generateDlStateUpdate(param: Param): String = {
    val Param(_, field) = param
    val template = s"""
         |
         |    if(monitorState.prevStateExists()) {
         |        monitorState.currState.$field = $field;
         |        monitorState.currStateMap["$field"] = true;
         |    } else {
         |        monitorState.prevState.$field = $field;
         |        monitorState.prevStateMap["$field"] = true;
         |    }
         |
         |""".stripMargin

    CFunction(None, "void", "update_" + param.pName, param :: Nil, template).toString + "\n"
  }

  def generateDlStateUpdatesFromModel(model:String): String = {
    val logicalVariables: List[Param] = ModelPlexConnector.getStateVarsFromModel(model)
                                                         .map(_.toString())
                                                         .map(v => Param("long double", v))
    logicalVariables.map(generateDlStateUpdate).mkString("\n")
  }

  def generateViolationCheckFunction(specName: String): String = {
      s"""
         |/* Violation check function */
         |bool check_violation( void ($specName::*pre_check)()
         |                    , void ($specName::*post_check)(bool)
         |                    ) {
         |    if(monitorState.isInitialized()) {
         |        (this->*pre_check)();
         |        bool verdict = modelplex_generated::monitorSatisfied( monitorState.prevState
         |                                                            , monitorState.currState
         |                                                            , &monitorState.params );
         |        monitorState.prevState = monitorState.currState;
         |        (this->*post_check)(verdict);
         |        return verdict;
         |    }
         |    return true;
         |}
         |
         |""".stripMargin.toString
  }

  def generateDlConstructorFromModel(specName:String, model:String): String = {
    val logicalVariables = ModelPlexConnector.getStateVarsFromModel(model).map(v => "\"" + v.toString() + "\"")
    val template =
      s"""
         |/**
         | * RV-Monitor Generated Constructor
         | * Function "initializeParams" must be present in spec
         | * And initialize state "monitorState.params"
         | **/
         |$specName() {
         |    initializeParams();
         |    vector<string> logicalVariables {${logicalVariables.mkString(",")}};
         |    monitorState.initialize(logicalVariables);
         |}
         |
         |""".stripMargin

    template.toString
  }

}
