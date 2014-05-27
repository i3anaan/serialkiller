package link;

public abstract class FrameLinkLayer extends LinkLayer {
    public abstract void sendFrame(byte[] data);
    public abstract byte[] readFrame();
    public abstract String toCoolString();
}
