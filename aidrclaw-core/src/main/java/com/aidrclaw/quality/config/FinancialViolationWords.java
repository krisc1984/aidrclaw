package com.aidrclaw.quality.config;

/**
 * 金融违规词库
 * 
 * 包含金融销售中禁止使用的词汇和表达
 * 
 * @author aidrclaw
 * @since 1.0.0
 */
public class FinancialViolationWords {
    
    /**
     * 一般违规词（承诺收益类）
     */
    public static final String[] NORMAL_VIOLATIONS = {
        "保本",
        "保收益",
        "保底",
        "稳赚",
        "稳赚不赔",
        "零风险",
        "无风险",
        "绝对收益",
        "固定收益",
        "预期收益率",
        "业绩基准",
        "参考收益率"
    };
    
    /**
     * 严重违规词（虚假承诺类）
     */
    public static final String[] SERIOUS_VIOLATIONS = {
        "刚性兑付",
        "保本保息",
        "保本保收益",
        "存款替代",
        "存款级别",
        "银行担保",
        "国家担保",
        "政府担保",
        "100% 收益",
        "翻倍",
        "一夜暴富",
        "稳赢"
    };
    
    /**
     * 获取所有违规词
     */
    public static String[] getAllViolations() {
        String[] all = new String[NORMAL_VIOLATIONS.length + SERIOUS_VIOLATIONS.length];
        System.arraycopy(NORMAL_VIOLATIONS, 0, all, 0, NORMAL_VIOLATIONS.length);
        System.arraycopy(SERIOUS_VIOLATIONS, 0, all, NORMAL_VIOLATIONS.length, SERIOUS_VIOLATIONS.length);
        return all;
    }
}
