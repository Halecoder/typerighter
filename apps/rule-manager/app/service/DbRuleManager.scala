package service

import com.gu.typerighter.model.{BaseRule, Category, LTRule, LTRuleXML, RegexRule, RuleResource, TextSuggestion}
import db.DbRule

import java.util.regex.Pattern

object DbRuleManager {
  def baseRuleToDbRule(rule: BaseRule): DbRule = {
    rule match {
      case RegexRule(id, category, description, suggestions, replacement, regex) => DbRule(
        id = None,
        ruleType = "regex",
        pattern = regex.toString(),
        category = Some(category.name),
        description = Some(description),
        replacement = replacement.map(_.text),
        ignore = false,
        googleSheetId = Some(id),
      )
      case LTRuleXML(id, xml, category, description) => DbRule(
        id = None,
        ruleType = "languageTool",
        pattern = xml,
        category = Some(category.name),
        description = Some(description),
        replacement = None,
        ignore = false,
        googleSheetId = Some(id),
      )
    }
  }

  def dbRuleToBaseRule(rule: DbRule): BaseRule = {
    rule.ruleType match {
      case "regex" => RegexRule(
        id = rule.googleSheetId.get,
        category = Category(id = rule.category.get, name = rule.category.get),
        description = rule.description.get,
        suggestions = List.empty,
        replacement = rule.replacement.map(TextSuggestion(_)),
        regex = rule.pattern.r,
      )
      case "languageTool" => LTRuleXML(
        id = rule.googleSheetId.get,
        category = Category(id = rule.category.get, name = rule.category.get),
        description = rule.description.get,
        xml = rule.pattern,
      )
    }
  }

  def overwriteAllRules(rules: RuleResource): RuleResource = {
    rules.rules.grouped(500)
      .foreach( batch => DbRule.batchInsert(batch.map(baseRuleToDbRule)))

    val persistedRules = RuleResource(rules= DbRule.findAll().map(dbRuleToBaseRule), ltDefaultRuleIds = List.empty)
    val expectedRules = rules.copy(ltDefaultRuleIds = List.empty)



    if (persistedRules != expectedRules) {
      val allRules = persistedRules.rules.zip(expectedRules.rules);
      allRules.take(5).foreach { case (persistedRule, expectedRule) =>
        if (persistedRule != expectedRule) {
          println(s"Persisted rule: $persistedRule")
          println(s"Expected rule: $expectedRule")
        }
      }
      throw new Exception("Failed to persist rules")
    }
    persistedRules
  }
}
