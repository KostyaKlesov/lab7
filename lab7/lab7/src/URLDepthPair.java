import java.net.*;
// связывает юрл и глубину
public class URLDepthPair {
    public URL urlObj; // описываем переменную, которая хранит url
    private int depth;
    public URLDepthPair(String url, int depth) throws MalformedURLException {
        this.urlObj = new URL(url); // берем url и переделываем в другой объект
        this.depth = depth;  // берем depth
    }
    // геттер

    public int getDepth() {
        return depth;
    }
    public String toString(){ // для того чтобы работал println с pair
        return urlObj.toString() + " " + depth;
    }
}
