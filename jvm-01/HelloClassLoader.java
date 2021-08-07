package jvm;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @description:
 * @author: MengyuWu
 * @time: 2021/8/4 20:10
 */
public class HelloClassLoader extends ClassLoader{

    public static void main(String[] args) {
        try {
            Class<?> helloClass = new HelloClassLoader().findClass("Hello");
            Method method = helloClass.getMethod("hello");
            method.invoke(helloClass.newInstance());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
    protected Class<?> findClass(String name) throws ClassNotFoundException{
        String helloBase64 ="";
        byte[] bytes = loadFile("E:\\wmy\\wmy\\workspace\\JikeTimeProject\\src\\main\\java\\jvm\\Hello.xlass");
        for (int i=0;i<bytes.length;i++) {
            bytes[i]=(byte)(255-bytes[i]);
        }
        return defineClass(name,bytes,0,bytes.length);
    }

    /**
     *  读取文件字节流
     * @param path
     * @return
     */
    public static byte[] loadFile(String path){
        File file = new File(path);
        ByteArrayOutputStream bos = new ByteArrayOutputStream((int) file.length());
        BufferedInputStream in = null;
        try{
            in = new BufferedInputStream(new FileInputStream(file));
            int buf_size =1024;
            byte[] buffer = new byte[buf_size];
            int len=0;
            while(-1!=(len = in.read(buffer,0,buf_size))){
                bos.write(buffer,0,len);
            }
            return bos.toByteArray();
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                in.close();
            }catch (IOException e){
                e.printStackTrace();
            }
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
