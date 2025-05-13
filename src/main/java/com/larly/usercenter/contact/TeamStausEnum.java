package com.larly.usercenter.contact;

public enum TeamStausEnum {


    /**
     * 公开
     */
    PUBLIC(0,"公开"),
    /**
     * 私有
     */
    PRIVATE(1, "私有"),
    /**
     * 加密
     */
    Encryption(3,"加密");

    private Integer key;
    private String text;

//    根据key获取text
    public static String getTextByKey(Integer key){
        if(key == null) {
            return null;
        }
        TeamStausEnum[] values = TeamStausEnum.values();
        for (TeamStausEnum teamStausEnum : values) {
            if(teamStausEnum.getKey() == key){
                return teamStausEnum.getText();
            }

        }
        return null;
    }


    TeamStausEnum(Integer key, String text) {
        this.key = key;
        this.text = text;
    }

    public Integer getKey() {
        return key;
    }

    public String getText() {
        return text;
    }
}
