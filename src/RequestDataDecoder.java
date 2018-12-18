import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

public class RequestDataDecoder extends ReplayingDecoder<RequestData> {
    @Override
    protected void decode(ChannelHandlerContext ctx,
                          ByteBuf in, List<Object> out) throws Exception {

        RequestData data = new RequestData();
        data.type = in.readInt();
        data.size = in.readInt();
        data.data = new byte[data.size];
        in.readBytes(data.data);
    }

}
