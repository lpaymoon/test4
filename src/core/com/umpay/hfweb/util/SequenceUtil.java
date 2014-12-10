package com.umpay.hfweb.util;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Hashtable;
/*
 *  ******************  类说明  *********************
 * class       :  SequenceUtil
 * @author     :  孙善峰
 * @version    :  1.0  
 * description :  
 * @see        :                        
 * ***********************************************
 */
public class SequenceUtil {

    private static SequenceUtil su = null;
    private static final Hashtable serials = new Hashtable();
    private static final Hashtable fchannels = new Hashtable();

    /*
     * 
     */
    private SequenceUtil() {
    }

    /*
     *  ********************************************
     * method name   : getInstance 
     * description   : 
     * @return       : SequenceUtil
     * @param        : @return
     * modified      : 孙善峰 ,  2007-10-29
     * @see          : 
     * *******************************************
     */
    public static synchronized SequenceUtil getInstance() {
        if (su == null)
            su = new SequenceUtil();
        return su;
    }
    /*
     *  ********************************************
     * method name   : getSequence4File 
     * description   : 
     * @return       : long
     * @param        : @param name
     * @param        : @return
     * modified      : 孙善峰 ,  2007-10-29
     * @see          : 
     * *******************************************
     */
	public synchronized long getSequence4File(String name) {
        String appKey = name + ".seq";
        FileChannel fc = null;
        MappedByteBuffer serial = null;
        try {
        	fc = (FileChannel) fchannels.get(appKey);
        	serial = (MappedByteBuffer) serials.get(appKey);
            if (serial == null) {
                //获得一个可读写的随机存取文件对象
                RandomAccessFile RAFile = new RandomAccessFile(appKey, "rw");
                if (RAFile.length() < 8) {
                    RAFile.writeLong(0);
                }

                //获得相应的文件通道
                fc = RAFile.getChannel();

                //取得文件的实际大小，以便映像到共享内存
                int size = (int) fc.size();

                //获得共享内存缓冲区，该共享内存可读写
                serial = fc.map(FileChannel.MapMode.READ_WRITE, 0, size);
                
                fchannels.put(appKey, fc);
                serials.put(appKey, serial);
	        }
	        
	        FileLock flock = fc.lock();
	        serial.rewind();
	        long serno = serial.getLong();
            serno++;
	        serial.flip();
	        serial.putLong(serno);
	        flock.release();

	        return serno;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }
	
	/*
	 *  ********************************************
	 * method name   : formatSequence 
	 * description   : 
	 * @return       : String
	 * @param        : @param seq
	 * @param        : @param width
	 * @param        : @return
	 * modified      : 孙善峰 ,  2007-10-29
	 * @see          : 
	 * *******************************************
	 */
	   public static String formatSequence(long seq, int width) {
	        Long lg = new Long(seq);
	        String is = lg.toString();
	        if (is.length() < width) { // 需要前面补'0'
	            while (is.length() < width)
	                is = "0" + is;
	        } else { // 需要将高位丢弃
	            is = is.substring(is.length() - width, is.length());
	        }
	        return is;
	    }
}
