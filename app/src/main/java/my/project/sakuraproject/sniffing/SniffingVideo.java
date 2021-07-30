package my.project.sakuraproject.sniffing;
import android.os.Parcel;
import android.os.Parcelable;

public class SniffingVideo implements Parcelable {

    private String pseudoType;//伪类型，通过后缀判断的
    private String type; // 文件类型，不一定能获取成功
    private String url; //视频连接
    private int length;//文件长度，不一定能获取成功
    private boolean isSuffix;//.xxx 后缀是否在最后
    private boolean isRedirect;// 是否为重定向的url  如 http://xxx.xxx.xxx?url=http://xx.xx.xx

    public SniffingVideo(String url, String pseudoType) {
        this(url, pseudoType, -1, "");
    }

    public SniffingVideo(String url, String pseudoType, int length, String type) {
        this.url = url;
        this.length = length;
        this.type = type;
        this.pseudoType = pseudoType.substring(1);
        this.isRedirect = url != null && url.contains("=") && url.lastIndexOf("http") != 0;
        this.isSuffix = url != null && url.lastIndexOf(pseudoType) == url.length() - pseudoType.length();
    }

    protected SniffingVideo(Parcel in) {
        pseudoType = in.readString();
        type = in.readString();
        url = in.readString();
        length = in.readInt();
        isSuffix = in.readByte() != 0;
        isRedirect = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(pseudoType);
        dest.writeString(type);
        dest.writeString(url);
        dest.writeInt(length);
        dest.writeByte((byte) (isSuffix ? 1 : 0));
        dest.writeByte((byte) (isRedirect ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SniffingVideo> CREATOR = new Creator<SniffingVideo>() {

        @Override
        public SniffingVideo createFromParcel(Parcel in) {
            return new SniffingVideo(in);
        }

        @Override
        public SniffingVideo[] newArray(int size) {
            return new SniffingVideo[size];
        }

    };

    public boolean isHtml() {
        return type != null && type.contains("text/html");
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        if (url == null) {
            return "";
        }
        return url;
    }

    public long getLength() {
        return length;
    }

    public boolean isSuffix() {
        return isSuffix;
    }

    public boolean isRedirect() {
        return isRedirect;
    }

    public String getPseudoType() {
        return pseudoType;
    }

    @Override
    public String toString() {
        return "SniffingVideo{" +
                "pseudoType='" + pseudoType + '\'' +
                ", type='" + type + '\'' +
                ", url='" + url + '\'' +
                ", length=" + length +
                ", isSuffix=" + isSuffix +
                ", isRedirect=" + isRedirect +
                '}';
    }

}