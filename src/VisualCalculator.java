import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;



import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

// ---------------------------------------------------------------
// Classe que cria uma Frame principal, onde se situam os comandos
// de manipula��o de imagem. Implementa a interface ActionListener
// para lidar com os eventos produzidos pelos bot�es.
// ---------------------------------------------------------------
class VisualCalculator extends Frame implements ActionListener {
	private static final long serialVersionUID = 1L;

	// Vari�veis globais de apoio
	// Aten��o: E se eu quiser ter m�ltiplas imagens?
	// Isto deve estar na classe ImageFrame!
	private Image image;
	private int sizex;
	private int sizey;
	private int matrix[];
	ImagePanel imagePanel; // E se eu quiser m�ltiplas janelas?
	private Processor processor;
	private SegmentedFile segFile;
	private Database dataBase;
	private ArrayList<Blob> blobList;
	private String filename = "exp3.jpg";
	private char symbol = '?';
	private String expressionToCalculate;
	private String filenameToSave = "res.jpg";
	
	public static void main(String args[]) throws IOException{
		new VisualCalculator();
	}

	// Construtor
	public VisualCalculator() throws IOException{
		
		//open database
		FileInputStream si;
		ObjectInputStream ois = null;
		try {
		    si = new FileInputStream("database.ser");
		    ois = new ObjectInputStream(si);
	        dataBase = (Database) ois.readObject();
	        System.out.println("entrou na database, tem "+dataBase.getSize() + " DatabaseItems");
	        dataBase.checkSize();
		} catch (Exception e) {
		    if(e instanceof FileNotFoundException){
		    	FileOutputStream fot = new FileOutputStream("database.ser", false);
		    	System.out.println("criou database");
		    	dataBase = new Database();
		    	ObjectOutputStream oos = new ObjectOutputStream(fot);
		    	oos.writeObject(dataBase);
		    	oos.close();
		    }
		    else
		    	e.printStackTrace();
		} finally {
		    if(ois != null){
		        ois.close();
		    } 
		}
		
		
		processor = new Processor();
		// Lidar com o evento de Fechar Janela
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		// Sinalizar que nao existe nenhum ImagePanel
		imagePanel = null;

		// Criar bot�es 
		this.setLayout(new GridLayout(4,1,1,1));

		Button button = new Button("Open File");
		button.setVisible(true);
		button.addActionListener(this);
		add(button);		

		button = new Button("Apply Filter");
		button.setVisible(true);
		button.addActionListener(this);
		add(button);
		
		button = new Button("Apply Segmentation");
		button.setVisible(true);
		button.addActionListener(this);
		add(button);
		
		button = new Button("Remove Shadow");
		button.setVisible(true);
		button.addActionListener(this);
		add(button);
		
		button = new Button("Apply FloodFill");
		button.setVisible(true);
		button.addActionListener(this);
		add(button);
		
		button = new Button("Calculate");
		button.setVisible(true);
		button.addActionListener(this);
		add(button);
		
		button = new Button("Add to Database");
		button.setVisible(true);
		button.addActionListener(this);
		add(button);
		
		button = new Button("Save File");
		button.setVisible(true);
		button.addActionListener(this);
		add(button);
		
		button = new Button("Resize");
		button.setVisible(true);
		button.addActionListener(this);
		add(button);



		pack();

		// Janela principal 	
		setLocation(30,100);
		setSize(300,400);
		setVisible(true);
	}


	// O utilizador carregou num bot�o
	public void actionPerformed (ActionEvent myEvent)
	{
		// Qual o bot�o premido?
		Button pressedButton = (Button)myEvent.getSource();
		String nomeBotao = pressedButton.getActionCommand();

		// Realizar ac��o adequada
		if (nomeBotao.equals("Open File")) openFile();
		else if (nomeBotao.equals("Apply Filter")) applyFilter();
		else if (nomeBotao.equals("Apply Segmentation")) applySegmentation();
		else if (nomeBotao.equals("Apply FloodFill")) applyFloodFill();
		else if (nomeBotao.equals("Remove Shadow")) removeShadows();
		else if (nomeBotao.equals("Save File")) saveFile();
		else if (nomeBotao.equals("Add to Database"))
			try {
				saveToDatabase();
			} catch (IOException e) {
				e.printStackTrace();
			}
		else if (nomeBotao.equals("Resize")) resizeImage();
		else if (nomeBotao.equals("Calculate")) calculate();
		
	}

	// Abrir um ficheiro de Imagem
	private void openFile() {
		LoadImage(filename);

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
		imagePanel.setLocation(250, 5);
		imagePanel.setSize(image.getWidth(null),image.getHeight(null));
		imagePanel.setVisible(true);
	}

	public void applyFilter(){
		
		NoiseFilter noise = new NoiseFilter();
		
		long startTime = System.currentTimeMillis();
		
		int matrix[] = noise.medianFilter(3,3, image);
		long stopTime = System.currentTimeMillis();	
		long elapsedTime = stopTime - startTime;
		System.out.println("segundos: " + elapsedTime*0.001 );
		image = createImage(new MemoryImageSource(sizex, sizey, matrix, 0, sizex));
		// Carregar a imagem no painel externo de visualizacao
		imagePanel.newImage(image);
	}
	
	public void expandImage(){
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
		
		
	}
	
	public void resizeImage() {
		int sizex = image.getWidth(null);
		int sizey = image.getHeight(null);
		
		
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
		
		int binaryMatrix[][] = new int[sizey][sizex];
		for (int i=0; i<sizey;i++){
			for(int j=0; j<sizex; j++){
					binaryMatrix[i][j]=0;
				}
					
			}
		
		double sx = 300.f/sizex;
		double sy =500.f/sizey;
		
		matrix = processor.resize(binaryMatrix, matrix, sx, sy);
		image = createImage(new MemoryImageSource((int)(sx*sizex), (int)(sy*sizey), matrix, 0, (int)(sx*sizex)));
		imagePanel.newImage(image);
		
		
		System.out.println("W: " + image.getWidth(null) + " H: " +image.getHeight(null));
	}
	
	
	public void removeShadows() {
		segFile = processor.eliminateLateraShadow(segFile);
		image = createImage(new MemoryImageSource((int)(sizex), (int)(sizey), segFile.getRepresentation(), 0, (int)(sizex)));
		
		imagePanel.newImage(image);
	}
	
	public void applySegmentation(){
		segFile = processor.segment(image);
		image = createImage(new MemoryImageSource(sizex, sizey, segFile.getRepresentation(), 0, sizex));
		imagePanel.newImage(image);
	}
	
	public void applyFloodFill(){
		blobList = processor.floodFill(segFile.getBinaryMatrix());
		System.out.println(blobList.size());
		Collections.sort(blobList);
		blobList = processor.removeFalseBlobs(blobList);
		System.out.println(blobList.size());
		
	}
	
	public void calculate(){
		//sort blobs by position
		blobList = Blob.sortByPosition(blobList);
		//resizeBlobs
		for(Blob b : blobList){
			float sx= 300.f/(b.getP2x()-b.getP1x());
			float sy = 500.f/(b.getP2y()-b.getP1y());			
			int colorMatrix[] = processor.resize(b.getContent(), b.getMatrixImage(), sx, sy);
			int binaryMatrix [][] = new int[(int)((b.getP2y()-b.getP1y())*sy)][(int)((b.getP2x()-b.getP1x())*sx)];
			int x=0;
			for (int i=0; i<binaryMatrix.length; i++){
				for(int j=0; j< binaryMatrix[0].length; j++){
					int vermelho = getRed(colorMatrix[x]);
					int verde = getGreen(colorMatrix[x]);
					int azul = getBlue(colorMatrix[x]);
					
					if(vermelho==255 && verde==255  && azul==255){
						binaryMatrix[i][j]=0;
					}
					else{
						binaryMatrix[i][j]=1;
					}
					x++;	
				}
			}
			b.setContent(binaryMatrix);
		}
		
		//correlation
		expressionToCalculate = new String("");
		for(Blob b : blobList){
			char c = dataBase.correlation(b);
			expressionToCalculate+=c;
		}
		if(expressionToCalculate.substring(expressionToCalculate.length()-2).equals("--"))
			expressionToCalculate = expressionToCalculate.substring(0,expressionToCalculate.length()-2);
		System.out.println(expressionToCalculate);
		
		//calculate expression result
		String result = Calculator.calculate(expressionToCalculate+" ");
		System.out.println(result);
		//display result in image
		
	}
	
	
	private void saveFile() {
		BufferedImage bi = new BufferedImage(image.getWidth(null),image.getHeight(null),BufferedImage.TYPE_INT_RGB);
		Graphics bg = bi.getGraphics();
		bg.drawImage(image, 0, 0, null);
		bg.dispose();
		try {
			ImageIO.write(bi, "jpg", new File(filenameToSave));
		} catch (IOException e) {
			System.err.println("Couldn't write output file!");
			return;
		}
	}

	public void saveToDatabase() throws IOException{
		SegmentedFile seg = processor.segment(image);
		Blob t = new Blob(seg.getBinaryMatrix(), 0, 0, sizex, sizey, sizex*sizey, 0);
		dataBase.addBlob(t,symbol);
		System.out.println("adicionado com sucesso, tem "+dataBase.getSize() + " DatabaseItems");	
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
	
	private int getRed(int color) { return (color >> 16) & 0xff; }
	private int getGreen(int color) { return (color >> 8) & 0xff; }
	private int getBlue(int color) { return color & 0xff; }
	private int makeColor(int red, int green, int blue) { return (255 << 24) | (red << 16) | (green << 8) | blue; }
}
