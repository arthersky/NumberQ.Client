package langotec.numberq.client.dbConnect;

import android.util.Log;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import langotec.numberq.client.Store;
import langotec.numberq.client.login.Member;
import langotec.numberq.client.menu.Menu;

public class ReadFile {
    private String myfile;
    private File dir;
    private Member member;
    public ReadFile(File dir, String myfile, Member member){
        Log.e("readFile", "dir:" + dir + "/" + myfile + "   member:"+member.toString());
        this.dir = dir;
        this.myfile = myfile;
        this.member = member;
    }

    public ReadFile(File dir, String myfile, Store store){
        this.dir = dir;
        this.myfile = myfile;
    }

    public ReadFile(File dir, String myfile, Menu menu){
        this.dir = dir;
        this.myfile = myfile;
    }

    public Boolean read() {
        File file = new File(dir, myfile);
        if(file.exists()){
            char[] buffer = new char[10]; //一次讀取一個位元
            FileReader fr = null;
            StringBuilder sb = new StringBuilder();
            try{
                fr = new FileReader(file);
                while(fr.read(buffer) !=-1){
                    sb.append(buffer);
                }
            } catch(IOException e){
            } finally{
                try{
                    fr.close();  // 關閉檔案
                } catch(IOException e){  }
            }
            final String result = sb.toString().trim();
            Log.e("result", result);
            if(result.equals("no record")){
                return false;
            }else{
                member = new parseJSON(result, member).parse();
                return true;
            }
        } else {
            return false;
        }
    }
}
