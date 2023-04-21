import React from 'react';
import {
    EuiSearchBarProps,
    EuiBasicTableColumn,
    EuiInMemoryTable,
    EuiTitle,
    EuiFlexItem,
    EuiButton,
    EuiFlexGroup,
    EuiLoadingSpinner,
    EuiButtonIcon,
    EuiFlexGrid,
    EuiForm,
    EuiFormRow,
    EuiFieldText,
} from '@elastic/eui';
import {useRules} from "./hooks/useRules";
import {css} from "@emotion/react";

const sorting = {
    sort: {
        field: 'description',
        direction: 'desc' as const,
    },
};

type Category = {
    name: string;
    id: string;
}

export type Rule = {
    type: string;
    id: string;
    description: string;
    replacement: {
      type: 'TEXT_SUGGESTION',
      text: string,
    };
    category: Category;
    enabled: boolean;
    regex: string;
}

const columns: Array<EuiBasicTableColumn<Rule>> = [
    {
        field: 'ruleType',
        name: 'Type',
    },
    {
        field: 'googleSheetId',
        name: 'ID'
    },
    {
        field: 'category',
        name: 'Category',
    },
    {
        field: 'pattern',
        name: 'Match',
    },
    {
      field: 'replacement',
      name: 'Replacement',
    },
    {
        field: 'description',
        name: 'Description'
    }
];

const RulesTable = () => {
    const {rules, isLoading, error, refreshRules, isRefreshing, setError} = useRules();
    const search: EuiSearchBarProps = {
        box: {
            incremental: true,
            schema: true,
        }
    };

    return <>
        <EuiFlexGroup>
            <EuiFlexItem grow={false} css={css`padding-bottom: 20px;`}>
                <EuiTitle>
                    <h1>Current rules</h1>
                </EuiTitle>
            </EuiFlexItem>
            <EuiFlexItem grow={false}>
                <EuiButton size="s" fill={true} color={"primary"} onClick={() => refreshRules()} isLoading={isRefreshing}>
                    Refresh{isRefreshing ? "ing" : ""} rules
                </EuiButton>
            </EuiFlexItem>
        </EuiFlexGroup>
        <EuiFlexGrid>
            {isLoading &&
                <EuiFlexItem grow={true} css={css`
                  align-content: center;
                  display: flex;
                  justify-content: center;
                  width: 100%;
                  padding-bottom: 20px;
                `}>
                    <EuiLoadingSpinner size="m"/>
                </EuiFlexItem>

            }
            {error &&
                <EuiFlexItem grow={true} style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    width: '100%',
                    backgroundColor: '#F8D7DA',
                    color: '#721C24',
                    flexDirection: 'row',
                    padding: '10px',
                    borderRadius: '5px',
                    marginBottom: '10px',
                    fontWeight: 'bold'
                }}>
                    <div>{`${error}`}</div>
                    <EuiButtonIcon
                        onClick={() => setError(undefined)}
                        iconType="cross"/></EuiFlexItem>

            }
        </EuiFlexGrid>

        <EuiFlexGroup>
            <EuiFlexItem grow={2}>
                {rules &&
                    <EuiInMemoryTable
                        tableCaption="Demo of EuiInMemoryTable"
                        items={rules}
                        columns={columns}
                        pagination={true}
                        sorting={sorting}
                        search={search}
                    />
                }
            </EuiFlexItem>

            <EuiFlexItem grow={1} css={css`
                  background-color: #D3DAE6;
                  padding-top: 12px;
                  padding-bottom: 48px;
                  padding-left: 12px;
                  padding-right: 48px;
                  border-radius: 4px;
                `}>
                <h2 style={{
                    fontFamily: 'Open Sans',
                    color: '#1A1C21',
                    fontWeight: '700',
                    paddingBottom: '20px'
                }}>RULE CONTENT</h2>
                <EuiForm component="form">
                    <EuiFormRow
                        label="Replacement"
                        helpText="What is the ideal term as per the house style?"
                    >
                        <EuiFieldText/>
                    </EuiFormRow>
                </EuiForm>
            </EuiFlexItem>
        </EuiFlexGroup>

    </>
}

export default RulesTable;
