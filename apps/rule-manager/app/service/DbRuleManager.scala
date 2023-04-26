package service

import com.gu.typerighter.lib.Loggable
import com.gu.typerighter.model.{
  BaseRule,
  Category,
  ComparableRegex,
  LTRule,
  LTRuleCore,
  LTRuleXML,
  RegexRule,
  RuleResource,
  TextSuggestion
}
import db.DbRule
import db.DbRule.autoSession
import scalikejdbc.DBSession

object DbRuleManager extends Loggable {
  object RuleType {
    val regex = "regex"
    val languageToolXML = "languageToolXML"
    val languageToolCore = "languageToolCore"
  }

  def baseRuleToDbRule(rule: BaseRule): DbRule = {
    rule match {
      case RegexRule(id, category, description, _, replacement, regex) =>
        DbRule.withUser(
          id = None,
          ruleType = RuleType.regex,
          pattern = Some(regex.toString()),
          category = Some(category.name),
          description = Some(description),
          replacement = replacement.map(_.text),
          ignore = false,
          googleSheetId = id,
          user = "Google Sheet"
        )
      case LTRuleXML(id, xml, category, description) =>
        DbRule.withUser(
          id = None,
          ruleType = RuleType.languageToolXML,
          pattern = Some(xml),
          category = Some(category.name),
          description = Some(description),
          replacement = None,
          ignore = false,
          googleSheetId = id,
          user = "Google Sheet"
        )
      case LTRuleCore(_, languageToolRuleId) =>
        DbRule.withUser(
          id = None,
          ruleType = RuleType.languageToolCore,
          googleSheetId = languageToolRuleId,
          ignore = false,
          user = "Google Sheet"
        )
      case _: LTRule =>
        throw new Error(
          "A languageTool-generated rule should not be available in the context of the manager service"
        )
    }
  }

  def dbRuleToBaseRule(rule: DbRule): Either[String, BaseRule] = {
    rule match {
      case DbRule(
            _,
            RuleType.regex,
            Some(pattern),
            replacement,
            Some(category),
            _,
            description,
            _,
            _,
            googleSheetId,
            _,
            _,
            _,
            _,
            _,
            _,
            _
          ) =>
        Right(
          RegexRule(
            id = googleSheetId,
            category = Category(id = category, name = category),
            description = description.getOrElse(""),
            suggestions = List.empty,
            replacement = replacement.map(TextSuggestion(_)),
            regex = new ComparableRegex(pattern)
          )
        )
      case DbRule(
            _,
            RuleType.languageToolXML,
            Some(pattern),
            _,
            Some(category),
            _,
            description,
            _,
            _,
            googleSheetId,
            _,
            _,
            _,
            _,
            _,
            _,
            _
          ) =>
        Right(
          LTRuleXML(
            id = googleSheetId,
            category = Category(id = category, name = category),
            description = description.getOrElse(""),
            xml = pattern
          )
        )
      case DbRule(
            _,
            RuleType.languageToolCore,
            _,
            _,
            _,
            _,
            _,
            _,
            _,
            googleSheetId,
            _,
            _,
            _,
            _,
            _,
            _,
            _
          ) =>
        Right(LTRuleCore(googleSheetId, googleSheetId))
      case other => Left(s"Could not derive BaseRule from DbRule for: $other")
    }
  }

  def getRules()(implicit session: DBSession = autoSession): List[DbRule] = DbRule.findAll()

  def createRuleResourceFromDbRules(dbRules: List[DbRule]): Either[List[String], RuleResource] = {
    val (failedDbRules, successfulDbRules) = dbRules
      .filter(_.ignore == false)
      .map(dbRuleToBaseRule)
      .partitionMap(identity)

    failedDbRules match {
      case Nil      => Right(RuleResource(successfulDbRules))
      case failures => Left(failures)
    }
  }

  def destructivelyDumpRulesToDB(
      incomingRules: List[DbRule]
  )(implicit session: DBSession = autoSession): Either[List[String], List[DbRule]] = {
    DbRule.destroyAll()

    incomingRules
      .grouped(100)
      .foreach(DbRule.batchInsert)

    val persistedRules = getRules()
    val rulesToCompare = persistedRules.map(_.copy(id = None))

    if (rulesToCompare == incomingRules) {
      Right(persistedRules)
    } else {
      val allRules = rulesToCompare.zip(incomingRules)
      log.error(s"Persisted rules differ.")

      allRules.take(10).foreach { case (ruleToCompare, expectedRule) =>
        log.error(s"Persisted rule: $ruleToCompare")
        log.error(s"Expected rule: $expectedRule")
      }

      Left(
        List(
          s"Rules were persisted, but the persisted rules differ from the rules we received from the sheet."
        )
      )
    }
  }
}
