package org.komparator.mediator.ws;

import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.komparator.mediator.ws.cli.MediatorClient;
import org.komparator.mediator.ws.cli.MediatorClientException;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;

public class LifeProof extends TimerTask {
	
	/** Date formatter used for outputting timestamps in ISO 8601 format */
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	
	private MediatorEndpointManager endpoint = null;
	
	private String wsName = null;
	
	private String wsURL = null;
	
	private String uddiURL = null;
	
	private String lastRegisteredTimestamp = null;
	
	private Date prevDate = null;
	
	private long networkDelay;
	
	private long regularDelay = 5000;
	
	private long delayMargin = 2500; // no caso de estarem completamente desfazadas e haver delay na rede
	
	private long threadDelay = 0;
	
	private boolean primaryOnline = false;
	// alterar aqui e na pom
	private static final String SECUNDARY_URL = "http://localhost:8072/mediator-ws/endpoint";
	
	public LifeProof(MediatorEndpointManager endpoint, String wsURL, String uddiURL) {
		this.endpoint = endpoint;
		this.wsName = endpoint.getWsName();
		this.wsURL = wsURL;
		this.uddiURL = uddiURL;
	}
	
	public void run() {

		System.out.println(this.getClass() + " running...");
		MediatorClient secundaryClient;
		System.out.println("");
		System.out.println(wsName);
		System.out.println(wsURL);
		System.out.println(uddiURL);
		System.out.println("");
		// - A minha forma de garantir que o segundo servidor nao entra neste if e usando agora o wsURL
		//   dado que este e uma String estatica e final. Ou seja uso-o de forma a garantir que ele nao
		//   e ele proprio, de forma a nao estabelecer uma conexao como ele mesmo (que por acaso da).
		if(this.wsName != null && !this.wsURL.equals(SECUNDARY_URL)) {
			try {
				secundaryClient = new MediatorClient(SECUNDARY_URL);
				// ctr++; 
				// if(ctr == 3) 
				// Thread.sleep(10000); 
				// System.out.println("CTR :" + ctr);
				secundaryClient.imAlive();
				System.out.printf("Life proof sent to %s%n", SECUNDARY_URL);
				
				Object lockShopResults = MediatorPortImpl.lockShopResults;
				synchronized (lockShopResults) {
					System.out.println("Entrou no trinco");
					List<ShoppingResultView> shopResults = MediatorPortImpl.lastRegisteredShopResults;
					
					if (!shopResults.isEmpty()) {
						System.out.print("ShopResults size: ");
						System.out.println(shopResults.size());
						String shopResultsUpdatedNonce = MediatorPortImpl.shopResultsUpdateNonce;
						secundaryClient.updateShopHistory(shopResults, shopResultsUpdatedNonce);
						System.out.printf("Latest shopping results sent to %s%n", SECUNDARY_URL);
						shopResults.clear();
					}
					else {
						System.out.println("ShopResults is empty!");
					}
				}
				
				Object lockCartView = MediatorPortImpl.lockCartView;
				synchronized(lockCartView) {
					System.out.println("Entrou no trinco");
					List<CartView> cartViewList = MediatorPortImpl.lastRegisteredCartViews;	
					
					if (!cartViewList.isEmpty()) {
						System.out.print("CartViewList Size: ");
						System.out.println(cartViewList.size());
						String cartUpdateNonce = MediatorPortImpl.cartUpdateNonce;
						secundaryClient.updateCart(cartViewList, cartUpdateNonce);
						System.out.printf("Latest shopping carts sent to %s%n", SECUNDARY_URL);
						cartViewList.clear();
					}
					else {
						System.out.println("CartViewList is empty!");
					}
				}
				
			} catch (MediatorClientException e) {
				System.out.println("Caught exception while communicating with the backup server...");
				System.out.printf("Continue normal processing of %s thread...", this.getClass());
			} catch (Exception e) {
				System.out.println("Secundary Mediator not there");
				System.out.printf("Continue normal processing of %s thread...", this.getClass());
			}
		}
		else if (this.wsName != null && this.wsURL.equals(SECUNDARY_URL)) {
			System.out.println("Secundary server is now the primary server.");
		}
		else {
			this.primaryOnline = MediatorPortImpl.primaryOnline;
			
			if (primaryOnline) {
							
				try {
					this.lastRegisteredTimestamp = MediatorPortImpl.lastRegisteredTimestamp;
					Date timestampDate = dateFormatter.parse(lastRegisteredTimestamp);
					System.out.print("LastRegisteredTimestamp: ");
					System.out.println(lastRegisteredTimestamp);
					
					Date actualDate = new Date();
					System.out.print("ActualDate: ");
					System.out.println(dateFormatter.format(actualDate));
					
					// ora viva
					if (prevDate != null) {
						System.out.print("PreviousDate: ");
						System.out.println(dateFormatter.format(prevDate));
						networkDelay = actualDate.getTime() - prevDate.getTime();

						System.out.print("Network delay: ");
						System.out.println(networkDelay);
						
						System.out.print("Network delay - Average Thread delay: ");
						System.out.println(networkDelay - threadDelay);
						if (networkDelay - threadDelay < regularDelay + delayMargin) {
							System.out.println("Primary Server running...");
							prevDate = timestampDate; 
						}
						else {
							System.out.println("Primary Server just crashed!");
							try {
								System.out.println("Taking Primary server's place...");
								this.wsName = "T14_Mediator";
								System.out.printf("Publishing '%s' to UDDI at %s%n", wsName, uddiURL);
								/*UDDINaming uddiNaming = endpoint.getUddiNaming();
								uddiNaming = new UDDINaming(this.uddiURL);
								uddiNaming.rebind(this.wsName, this.wsURL); */
								endpoint.setWSName(this.wsName);
								endpoint.setWSURL(this.wsURL);
								endpoint.setUDDIURL(this.uddiURL);
								endpoint.publishToUDDI();
								
							} catch (Exception e) {
								System.out.printf("Caught exception when binding to UDDI: %s%n", e);
							} 
						}	
					}
		
					else {
						System.out.print("PreviousDate: ");
						System.out.println("first value not yet registed...");
						prevDate = timestampDate;
						
						// - Queria adicionar isto ao calculo da equacao mas ja estou meio confuso, depois se quisermos 
						//   pensamos nisto melhor. Isto e o delay que a thread causa, pois a leitura da data actual
						//   e do timestamp podem estar desfazados devido ao periodo de 5 em 5 segundos da thread.
						// - De qualquer forma este valor ja esta a ser amortecido no valor de margem do delay.
						// - Isto tem de ser pensado melhor pois este valor deveria ser descontado no calculo do delay
						//   da rede. No entanto a forma como esta aqui programado causa problemas. Facam um grafico
						//   e percebam o que estou a dizer.
						// - Por agora isto esta so a imprimir para verem como o delay do desfazamento da thread afecta
						//   o calculo do network delay.
						
						// - Agora calculo isto como o delay medio para todo a thread, mas isto pode ser feito dinamicamente
						//   que e o que eu quero. Mas por agora assim safa. Melhor do que por um tempo de tolerancia grande
						//   como estava.
						threadDelay = actualDate.getTime() - timestampDate.getTime();
					}
					
					System.out.print("Average Thread delay: ");
					System.out.println(threadDelay);
	
				} catch (ParseException e) {
					System.out.println("Date parsing gone wrong");
				}			
			}	
			
			else {
				System.out.println("Primary server not online as of yet...");
			}	
		}
	}
	
}
