package CarlyAdmin;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;

import CarlyAdmin.manager.ConfigurationManager;
import CarlyAdmin.manager.StatisticsManager;
import CarlyAdmin.parser.CarlyAdminMsg;
import CarlyAdmin.parser.CarlyResponse;

public class CarlyAdminHandler implements TCPProtocol {
	
	private StatisticsManager statManager;
	private ConfigurationManager configManager;

	public CarlyAdminHandler(StatisticsManager stat,
			ConfigurationManager config) {
		this.statManager = stat;
		this.configManager = config;
	}

	@Override
	public void handleAccept(SelectionKey key) throws IOException {
		SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
		channel.configureBlocking(false);
		CarlyAdminMsg msg = new CarlyAdminMsg(statManager, configManager);
		channel.register(key.selector(), SelectionKey.OP_READ, msg);
	}

	@Override
	public void handleRead(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		CarlyAdminMsg msg = (CarlyAdminMsg) key.attachment();
		final long bytesRead = channel.read(msg.getBuffer());
		if(bytesRead == -1){
			channel.close();
		}else{
			msg = msg.handleRead();
			if(msg.isFinished()){
				key.interestOps(SelectionKey.OP_WRITE);
			}
		}
	}

	@Override
	public void handleWrite(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		CarlyAdminMsg msg = (CarlyAdminMsg) key.attachment();
		if(msg.isFinished()){
			CarlyResponse response = new CarlyResponse(msg);
			String carlyAns = response.getCarlyAns();
			ByteBuffer buffer = ByteBuffer.wrap(carlyAns.getBytes());
			channel.write(buffer);
			channel.close();
		}
	}

}
