import {useEffect, useState} from "react";
import {EuiFormRow, EuiComboBox} from "@elastic/eui";
import React from "react";
import { PartiallyUpdateRuleData } from "./RuleForm";
import { existingTags } from "../constants/constants";
import {DraftRule} from "./hooks/useRule";
import {TagMap} from "./hooks/useTags";

type TagOption = { label: string, value?: number }

export const TagsSelector = ({tags, ruleData, partiallyUpdateRuleData}: {
    tags: TagMap,
    ruleData: DraftRule,
    partiallyUpdateRuleData: PartiallyUpdateRuleData,
}) => {
    const options = tags ? Object.values(tags).map(tag => ({ label: tag.name })) : [];
    const tagOptions = ruleData.tags.map(tag => ({label: tags[tag].name, value: tags[tag].id}));
    const [selectedTags, setSelectedTags] = useState<TagOption[]>(tagOptions);

    const onChange = (selectedTags: TagOption[]) => {

    };

    useEffect(() => {
        const newTags = selectedTags.map(tag => tag.value!)
        partiallyUpdateRuleData({tags: newTags})
    }, [selectedTags])

    return (
        <EuiFormRow label='Tags' fullWidth={true}>
            <EuiComboBox<number>
                options={options}
                selectedOptions={tagOptions}
                onChange={options => setSelectedTags(options)}
                isClearable={true}
                isCaseSensitive
                fullWidth={true}
            />
        </EuiFormRow>
    );
}
