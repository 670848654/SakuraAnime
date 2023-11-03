package my.project.sakuraproject.config;

public enum SettingEnum {
    VIDEO_PLAYER("请选择视频播放器", new String[]{"内置","外置"}),
    VIDEO_PLAYER_KERNEL(  "请选择播放器内核", new String[]{"ExoPlayer", "IjkPlayer"}),
    CHECK_APP_UPDATE("启动时是否检查更新", new String[]{"开启", "关闭"}),
    CHECK_AMINE_UPDATE( "是否开启追番更新检测", new String[]{"开启", "关闭"}),
    VIDEO_PLAYER_DANMU( "是否开启播放器弹幕功能", new String[]{"开启", "关闭"});

    private String dialogTitle;

    private String[] items;

    SettingEnum(String dialogTitle, String[] items) {
        this.dialogTitle = dialogTitle;
        this.items = items;
    }

    public String getDialogTitle() {
        return dialogTitle;
    }

    public String[] getItems() {
        return items;
    }
}
