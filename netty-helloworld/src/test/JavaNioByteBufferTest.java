import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaNioByteBufferTest {
    @Test
    public void testCreateByteBuffer(){
        CharBuffer heapBuffer = CharBuffer.allocate(11);
        assertEquals(11, heapBuffer.capacity());
        assertEquals(false, heapBuffer.isDirect());

        ByteBuffer directBuffer = ByteBuffer.allocateDirect(11);
        assertEquals(11, directBuffer.capacity());
        assertEquals(true, directBuffer.isDirect());

        int[] array = {1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 0};
        IntBuffer intHeapBuffer = IntBuffer.wrap(array);
        assertEquals(11, intHeapBuffer.capacity());
        assertEquals(false,intHeapBuffer.isDirect());
    }

    @Test
    public void testByteBufferTest1(){
        ByteBuffer firstBuffer = ByteBuffer.allocate(11);
        System.out.println("바이트 버퍼 초깃값 : "+ firstBuffer);

        byte[] source = "Hello world".getBytes();
        firstBuffer.put(source);
        System.out.println("11바이트 기록 후 " + firstBuffer);
    }

    @Test
    public void testByteBufferTest2() {
        ByteBuffer firstBuffer = ByteBuffer.allocate(11);
        System.out.println("초기 상태 : " + firstBuffer);

        byte[] source = "Hello world!".getBytes();

        for(byte item : source) {
            firstBuffer.put(item);
            System.out.println("현재 상태 : " + firstBuffer);
        }
    }

    @Test
    public void testByteBufferTest3(){
        ByteBuffer firstBuffer = ByteBuffer.allocate(11);
        System.out.println("초기 상태 : " + firstBuffer);

        firstBuffer.put((byte) 1);
        System.out.println(firstBuffer.get());
        System.out.println(firstBuffer);
    }

    @Test
    public void testRightByteBuffer() {
        ByteBuffer firstBuffer = ByteBuffer.allocate(11);
        System.out.println("초기 상태 : " + firstBuffer);

        firstBuffer.put((byte) 1);
        firstBuffer.put((byte) 2);
        assertEquals(2, firstBuffer.position());

        firstBuffer.rewind();
        assertEquals(0, firstBuffer.position());
        assertEquals(1, firstBuffer.get());
        assertEquals(1, firstBuffer.position());
    }

    @Test
    public void testWriteByteBufferTest() {
        ByteBuffer firstBuffer = ByteBuffer.allocate(11);
        assertEquals(0, firstBuffer.position());
        assertEquals(11, firstBuffer.limit());

        firstBuffer.put((byte) 1);
        firstBuffer.put((byte) 2);
        firstBuffer.put((byte) 3);
        firstBuffer.put((byte) 4);
        assertEquals(4, firstBuffer.position());
        assertEquals(11, firstBuffer.limit());

        firstBuffer.flip();
        assertEquals(0, firstBuffer.position());
        assertEquals(4, firstBuffer.limit());
    }

    @Test
    public void testReadByteBuffer() {
        byte[] tempArray = {1, 2, 3, 4, 5, 0, 0, 0, 0, 0, 0};
        ByteBuffer firstBuffer = ByteBuffer.wrap(tempArray);
        assertEquals(0, firstBuffer.position());
        assertEquals(11, firstBuffer.limit());

        assertEquals(1, firstBuffer.get());
        assertEquals(2, firstBuffer.get());
        assertEquals(3, firstBuffer.get());
        assertEquals(4, firstBuffer.get());
        assertEquals(4, firstBuffer.position());
        assertEquals(11, firstBuffer.limit());

        firstBuffer.flip();
        assertEquals(0, firstBuffer.position());
        assertEquals(4, firstBuffer.limit());

        firstBuffer.get(3);

        assertEquals(0, firstBuffer.position());
    }
}
