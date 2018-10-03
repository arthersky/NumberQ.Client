package langotec.numberq.client.dbConnect;

import okhttp3.OkHttpClient;

// 因為OkHttpClient全App只需要宣告一個來使用,所以將其單例化
public class OkHttpClientSingleton extends OkHttpClient{

    private static OkHttpClientSingleton okHttpClient;

    // private constructor，這樣其他物件就沒辦法直接用new來取得新的實體
    private OkHttpClientSingleton(){}

    // 因為constructor已經private，所以需要另外提供方法讓其他程式調用這個類別
    public static OkHttpClientSingleton getInstance() {
        if(okHttpClient == null){
            synchronized (OkHttpClientSingleton.class){
                if(okHttpClient == null)
                    okHttpClient = new OkHttpClientSingleton();
            };
        }
        return okHttpClient;
    }
}
