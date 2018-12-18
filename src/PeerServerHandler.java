import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PeerServerHandler extends ChannelInboundHandlerAdapter { // (1)

    PeerServer server;
    public PeerServerHandler(PeerServer server) {
       this.server = server;
    }

    public static Logger logger = Logger.getLogger(PeerServerHandler.class.getName());
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            // Do something with msg
            RequestData request = (RequestData) msg;
            switch (request.type) {
                case 0:
                    handleHeartBeat(request);
                    break;
                case 1:
                    handleAddTransaction(ctx, request);
                    break;
                case 2:
                    handleBroadcastBlockchain(request);
                    break;
                default:
                    logger.log(Level.WARNING, "Unhandled msg: unknown type.");


            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }

    public void handleHeartBeat(RequestData request) {
        try{
           ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(request.data));
           this.server.membership = (ArrayList<Integer>) is.readObject();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void handleAddTransaction(ChannelHandlerContext ctx, RequestData request) {
        try{
            ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(request.data));
            Transaction transaction = (Transaction) is.readObject();
            NewChain.currentOutStandingBlock.addTransaction(transaction);
            if(NewChain.currentOutStandingBlock.transactions.size() > 2) {
                NewChain.currentOutStandingBlock.mineBlock(NewChain.difficulty);
                // send out blockchain
                RequestData toAll = new RequestData();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream os = new ObjectOutputStream(bos);
                toAll.type = 2; // BroadcastBlockchain
                os.writeObject(NewChain.blockchain);
                os.flush();
                toAll.data = bos.toByteArray();
                // TODO: Read more about response.
                ChannelFuture future = ctx.writeAndFlush(toAll);
                logger.log(Level.INFO, "Sending block chains to everybody.");
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }


    }
    public void handleBroadcastBlockchain(RequestData request) {
        try{
            ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(request.data));
            ArrayList<Block> foreignBlockchain = (ArrayList<Block>) is.readObject();
            if(NewChain.blockchain.size() < foreignBlockchain.size() && NewChain.isValidChain(foreignBlockchain)) {
                NewChain.blockchain = foreignBlockchain;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}