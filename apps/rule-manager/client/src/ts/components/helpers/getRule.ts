export const getRule = async (ruleId: number) => {
    const getRuleResponse = fetch(`${location}rules/${ruleId}`, {
         method: 'GET',
     });
     return getRuleResponse
 }