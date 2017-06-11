package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.IpPacket;
import org.pcap4j.packet.Packet;
import play.Logger;
import play.Logger.ALogger;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.LegacyWebSocket;
import play.mvc.Result;
import play.mvc.WebSocket;
import services.PcapInitializer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;

@Singleton
public class HomeController extends Controller {

	private static final ALogger LOGGER = Logger.of(HomeController.class);

	private final HttpExecutionContext httpExecutionContext;
	private final PcapInitializer pcapInitializer;

	@Inject
	public HomeController(HttpExecutionContext httpExecutionContext,
			PcapInitializer pcapInitializer) {
		this.httpExecutionContext = httpExecutionContext;
		this.pcapInitializer = pcapInitializer;
	}

	public Result index() {
		return ok(views.html.index.render());
	}

	// TODO support multiple clients
	// TODO use Akka Stream WebSockets
	public LegacyWebSocket<JsonNode> ether() {
		return WebSocket.whenReady((in, out) -> {
			// In extra thread write pcap to WebSocket.out
			Runnable r = () -> {
				pcapToWebSocket(pcapInitializer.getPcapHandle(), out);
			};
			CompletableFuture pcapToWebSocketFuture = CompletableFuture
					.runAsync(r, httpExecutionContext.current());

			// For each event received on the socket
			in.onMessage(System.out::println);

			// When the socket is closed
			in.onClose(() -> {
				pcapToWebSocketFuture.cancel(true);
				LOGGER.info("Closed WebSocket");
			});

			LOGGER.info(
					"Opened WebSocket and writing network interface {} into it",
					pcapInitializer.getNetworkInterfaceName());
		});
	}

	private void pcapToWebSocket(PcapHandle pcapHandle,
			WebSocket.Out<JsonNode> out) {
		while (true) {
			try {
				Packet packet = pcapHandle.getNextPacket();
				if (packet != null) {
					writePacketToWebSocketOut(packet, out, pcapHandle);
				}
			} catch (NotOpenException e) {
				break;
			}
		}
	}

	private void writePacketToWebSocketOut(Packet packet,
			WebSocket.Out<JsonNode> out, PcapHandle pcapHandle) {
		ObjectNode outNode = Json.newObject();
		outNode.put("timestamp", pcapHandle.getTimestamp().toString());
		fillNodeWithEthernetPacket(outNode, packet.get(EthernetPacket.class));
		fillNodeWithIpPacket(outNode, packet.get(IpPacket.class));
		out.write(outNode);
	}

	private void fillNodeWithEthernetPacket(ObjectNode outNode,
			EthernetPacket ethernetPacket) {
		if (ethernetPacket != null) {
			EthernetPacket.EthernetHeader header =
					ethernetPacket.getHeader();
			outNode.put("macSrcAddr", header.getSrcAddr().toString());
			outNode.put("macDstAddr", header.getDstAddr().toString());
			outNode.put("macType", header.getType().valueAsString());
		}
	}

	private void fillNodeWithIpPacket(ObjectNode outNode, IpPacket ipPacket) {
		if (ipPacket != null) {
			IpPacket.IpHeader header = ipPacket.getHeader();
			outNode.put("ipSrcAddr", header.getSrcAddr().toString());
			outNode.put("ipDstAddr", header.getDstAddr().toString());
			outNode.put("ipProtocol",
					header.getProtocol().valueAsString());
			outNode.put("ipVersion",
					header.getVersion().valueAsString());
		}
	}

}
