import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;
import java.io.*;

// ---------------------------------------------------------------
// Classe que cria uma Frame principal, onde se situam os comandos
// de manipula��o de imagem. Implementa a interface ActionListener
// para lidar com os eventos produzidos pelos bot�es.
// ---------------------------------------------------------------
class PSI extends Frame implements ActionListener {
	private static final long serialVersionUID = 1L;
	
	// Vari�veis globais de apoio
	// Aten��o: E se eu quiser ter m�ltiplas imagens?
	// Isto deve estar na classe ImageFrame!
	private Image image;
	private int sizex;
	private int sizey;;
	private int matrix[];
	ImagePanel imagePanel; // E se eu quiser m�ltiplas janelas?
	
	// Fun��o main cria uma instance din�mica da classe
	public static void main(String args[])
	{
		new PSI();
	}

	// Construtor
	public PSI()
	{
		// Lidar com o evento de Fechar Janela
		addWindowListener(new WindowAdapter() {
      		public void windowClosing(WindowEvent e) {
        		System.exit(0);
      		}
		});

		// Sinalizar que n�o existe nenhum ImagePanel
		imagePanel = null;
		
		// Criar bot�es 
		this.setLayout(new GridLayout(3,1,1,1));
		
		Button button = new Button("Abrir Ficheiro Imagem");
		button.setVisible(true);
		button.addActionListener(this);
		add(button);		

		button = new Button("Manipular Imagem");
		button.setVisible(true);
		button.addActionListener(this);
		add(button);		

		button = new Button("Guardar Imagem");
		button.setVisible(true);
		button.addActionListener(this);
		add(button);		
		
		pack();
		
		// Janela principal 	
		setLocation(100,100);
		setSize(100,150);
		setVisible(true);
	}
	
	
	// O utilizador carregou num bot�o
	public void actionPerformed (ActionEvent myEvent)
	{
		// Qual o bot�o premido?
		Button pressedButton = (Button)myEvent.getSource();
		String nomeBotao = pressedButton.getActionCommand();

		// Realizar ac��o adequada
		if (nomeBotao.equals("Abrir Ficheiro")) abrirFicheiro();
		else if (nomeBotao.equals("Manipular Imagem")) manipularImagem();
		else if (nomeBotao.equals("Guardar Imagem")) guardarImagem();
	}
	
	// Abrir um ficheiro de Imagem
	private void abrirFicheiro()
	{
		// Load Image - Escolher nome da imagem a carregar!
		// Bem mais interessante usar uma interface gr�fica para isto...
		LoadImage("lena.jpg");
	
		// Obter matriz da imagem
		// A vari�vel "matrix" fica com os valores de cada pixel da imagem
		// A dimens�o desta � determinada por "sizex" e "sizey"
		// Cada valor t�m 4 bytes. Estes correspondem invidividualmente a:
		// Transpar�ncia, Vermelho, Verde, Azul
		// Para aceder aos valores individuais:
		//		red = (color >> 16) & 0xff;
		//	    green = (color >> 8) & 0xff;
		//		blue = color & 0xff;
		sizex = image.getWidth(null);
		sizey = image.getHeight(null);
		matrix = new int[sizex*sizey];
		PixelGrabber pg = new PixelGrabber(image, 0, 0, sizex, sizey, matrix, 0, sizex);
		try {
		    pg.grabPixels();
		} catch (InterruptedException e) {
		    System.err.println("interrupted waiting for pixels!");
		    return;
		}
		if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
		    System.err.println("image fetch aborted or errored");
		    return;
		}
		
		// Visualizar imagem - Usar um Frame externo
		if (imagePanel==null) imagePanel = new ImagePanel(image);
		else imagePanel.newImage(image);
		imagePanel.setLocation(300, 200);
		imagePanel.setSize(image.getWidth(null),image.getHeight(null));
		imagePanel.setVisible(true);
	}

	// Exemplo de uma fun��o que manipula a imagem
	public void manipularImagem()
	{
		// Exemplo: Convers�o de uma imagem a cores, para uma imagem a preto e branco

		// Vari�veis de apoio
		int verde, vermelho, azul, cinzento;
		int x;

		// Ciclo que percorre a imagem inteira
		for (x=0; x < sizex*sizey; x++)
		{
			vermelho = getRed(matrix[x]);
			verde = getGreen(matrix[x]);
			azul = getBlue(matrix[x]);
			
			// Calcular luminosidade
			cinzento = (vermelho+verde+azul)/3;
			
			// Criar valor de cor
			matrix[x] = makeColor(cinzento, cinzento, cinzento);
		}
		
		// Ap�s a manipula�ao da matrix, � necess�rio criar o objecto gr�fico (image) 
		image = createImage(new MemoryImageSource(sizex, sizey, matrix, 0, sizex));
		
		// Carregar a imagem no painel externo de visualiza��o
		imagePanel.newImage(image);
	}

	// Fun��o de apoio que grava a imagem visualizada
	private void guardarImagem()
	{
		// Criar uma BufferedImage a partir de uma Image
		BufferedImage bi = new BufferedImage(image.getWidth(null),image.getHeight(null),BufferedImage.TYPE_INT_RGB);
	    Graphics bg = bi.getGraphics();
	    bg.drawImage(image, 0, 0, null);
	    bg.dispose();
	    
	    // Escrever ficheiro de sa�da
	    // Pq n�o implementar uma interface de escolha do nome?
		try {
			ImageIO.write(bi, "jpg", new File("resultado.jpg"));
		} catch (IOException e) {
		    System.err.println("Couldn't write output file!");
		    return;
		}
	}
	
	// Fun��o de apoio que carrega uma imagem externa
	public void LoadImage(String fileName) {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		image = toolkit.getImage(fileName);
		MediaTracker mediaTracker = new MediaTracker(this);
		mediaTracker.addImage(image, 0);
		try { mediaTracker.waitForID(0); }
		catch (InterruptedException ie) {};		
	}

	// Fun��es de apoio para extrair os valores de R, G e B de uma imagem.
	private int getRed(int color) { return (color >> 16) & 0xff; }
	private int getGreen(int color) { return (color >> 8) & 0xff; }
	private int getBlue(int color) { return color & 0xff; }
	private int makeColor(int red, int green, int blue) { return (255 << 24) | (red << 16) | (green << 8) | blue; }
}

//---------------------------------------------------------------
// Classe Frame de apoio para visualiza��o de uma imagem
//--------------------------------------------------------------- 
class ImagePanel extends Frame
{
	private static final long serialVersionUID = 1L;
	private Image image; 
	
	// Construtor
	public ImagePanel(Image newImage)
	{
		image = newImage;

		// Handle close window button
		addWindowListener(new WindowAdapter() {
	  		public void windowClosing(WindowEvent e) {
	    		System.exit(0);
	  		}
		});
	}

	// Carregar nova imagem no ImagePanel
	public void newImage(Image im)
	{
		image = im;
		repaint();
	}
	
	// Desenhar imagem 
	public void paint(Graphics g) {
		g.drawImage(image, 0, 0, null);
		super.paint(g);
	}
}