import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RequestDataEncoder extends MessageToByteEncoder<RequestData> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RequestData requestData, ByteBuf byteBuf) throws Exception {
        byteBuf.writeInt(requestData.type);
        byteBuf.writeInt(requestData.type);
        byteBuf.writeBytes(requestData.data);
    }
}
