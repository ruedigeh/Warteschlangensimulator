/**
 * Copyright 2020 Alexander Herzog
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ui.modeleditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import language.Language;
import tools.SetupData;
import ui.help.Help;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Erstellt basierend auf den HTML-Hilfe-Seiten zu den Elementen eine LaTeX-Dokumentation.
 * @author Alexander Herzog
 * @see ModelElementCatalog
 * @see ModelElementCatalogDescriptionBuilder#buildAll()
 */
public class ModelElementCatalogDescriptionBuilder {
	@SuppressWarnings("rawtypes")
	private final Class helpFolderRelativeClass;
	private final String helpFolder;
	private final String seePageString;
	private final String[] elementString;
	private final String[] removeRefs;
	private final boolean replaceQuotationMarks;

	/**
	 * Konstruktor der Klasse
	 * @param helpFolderRelativeClass	Klasse zu der der Ressourcenpfad relativ sein soll
	 * @param helpFolder	Relativer Ressourcenpfad zu den Hilfeseiten in der aktuellen Sprache
	 * @param seePageString	Text "siehe Seite" f�r Verweise
	 * @param elementString Texte "-Element", die bei "<code>&lt;a href="..."&gt;...&lt;/a&gt;-Element</code>" erkannt werden sollen
	 * @param removeRefs	Seitennamen zu denen keine Verlinkungen erfolgen sollen
	 * @param replaceQuotationMarks	Sollen Anf�hrungszeichen in die deutsche Variante �berf�hrt werden?
	 */
	public ModelElementCatalogDescriptionBuilder(@SuppressWarnings("rawtypes") final Class helpFolderRelativeClass, final String helpFolder, final String seePageString, final String[] elementString, final String[] removeRefs, final boolean replaceQuotationMarks) {
		this.helpFolderRelativeClass=helpFolderRelativeClass;
		if (helpFolder==null) this.helpFolder=""; else {
			if (helpFolder.endsWith("/")) this.helpFolder=helpFolder.substring(0,helpFolder.length()-1); else this.helpFolder=helpFolder;
		}
		this.seePageString=(seePageString==null)?"":seePageString;
		this.elementString=(elementString==null)?new String[0]:elementString;
		this.removeRefs=(removeRefs==null)?new String[0]:removeRefs;
		this.replaceQuotationMarks=replaceQuotationMarks;
	}

	/**
	 * Erstellt ein Bild f�r ein Modellelement
	 * @param element	Element f�r das ein Bild erstellt werden soll
	 * @param zoom	Zoomfaktor f�r die Darstellung im Bild
	 * @return	Bild des Modellelements
	 */
	public static BufferedImage getImage(final ModelElementPosition element, final double zoom) {
		final BufferedImage tempImage=new BufferedImage(200+2*Shapes.SHADOW_WIDTH,200+2*Shapes.SHADOW_WIDTH,BufferedImage.TYPE_4BYTE_ABGR);
		element.drawToGraphics(tempImage.getGraphics(),new Rectangle(Shapes.SHADOW_WIDTH,Shapes.SHADOW_WIDTH,200,200),zoom,false); /* damit getLowerRightPosition() einen korrekten Wert enth�lt */

		final Point p=element.getLowerRightPosition();
		final Dimension d=new Dimension(p.x,p.y);
		final Dimension size=new Dimension(
				(int)Math.round(d.width*zoom)+20,
				Math.max(25,(int)Math.round((d.height+Shapes.SHADOW_WIDTH)*zoom))+1
				);
		final Color backgroundColor=Color.WHITE;

		final JPanel panel=new JPanel(new BorderLayout());
		panel.setBackground(backgroundColor);

		final BufferedImage image=new BufferedImage(size.width,size.height,BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics g=image.getGraphics();
		if (SetupData.getSetup().antialias) ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		element.drawToGraphics(g,new Rectangle(0,0,size.width,size.height-1),zoom,false);

		return image;
	}

	/**
	 * Erstellt ein Bild welches untereinander Bilder mehrerer Modellelemente enth�lt
	 * @param elements	Elemente f�r die Bilder erstellt werden sollen
	 * @param zoom	Zoomfaktor f�r die Darstellung der Elemente in dem Bild
	 * @return	Bild welches die Modellelemente enth�lt
	 */
	public static BufferedImage getGroupImage(final ModelElementPosition[] elements, final double zoom) {
		final List<BufferedImage> images=new ArrayList<BufferedImage>();
		for (ModelElementPosition element: elements) images.add(getImage(element,zoom));

		int sumHeight=0;
		int maxWidth=0;
		for (BufferedImage image: images) {
			sumHeight+=image.getHeight();
			maxWidth=Math.max(maxWidth,image.getWidth());
		}

		final BufferedImage result=new BufferedImage(maxWidth,sumHeight,BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D g=result.createGraphics();
		int yPos=0;
		for (BufferedImage image: images) {
			g.drawImage(image,0,yPos,null);
			yPos+=image.getHeight();
		}

		return result;
	}

	/**
	 * Erstellt Bild f�r alle Modellelement-Vorlagengruppen
	 * @param zoom	Zoomfaktor f�r die Darstellung der Elemente in den Bildern
	 * @return	Zuordnung von Gruppennamen zu Bildern
	 */
	public static Map<String,BufferedImage> getGroupImages(final double zoom) {
		final Map<String,BufferedImage> result=new HashMap<>();

		final ModelElementCatalog catalog=ModelElementCatalog.getCatalog();
		for (Map.Entry<String,Map<String,ModelElementPosition>> entry: catalog.getAll().entrySet()) {
			final Map<String,ModelElementPosition> group=entry.getValue();
			final String[] names=group.keySet().toArray(new String[0]);
			Arrays.sort(names);
			final List<ModelElementPosition> elements=new ArrayList<>();
			for (String name: names) elements.add(group.get(name));
			result.put(entry.getKey(),getGroupImage(elements.toArray(new ModelElementPosition[0]),zoom));
		}

		return result;
	}

	private static File getImageFileName(final File folder, final int nr, final String name) {
		final StringBuilder fileName=new StringBuilder();
		fileName.append(String.format("%02d - ",nr));
		for (int i=0;i<name.length();i++) {
			final char c=name.charAt(i);
			if ((c>='A' && c<='Z') || (c>='a' && c<='z') || (c>='0' && c<='9') || "�������-_#+-\"".contains(""+c)) fileName.append(c); else fileName.append(' ');
		}
		fileName.append(".png");

		return new File(folder,fileName.toString());
	}

	private static boolean saveAllStationsImage(final File folder, final List<String> titleList, final List<BufferedImage> imageList) {
		int sumWidth=0;
		int maxHeight=0;
		final int titleHeight=15;

		for (BufferedImage image: imageList) {
			sumWidth+=image.getWidth();
			maxHeight=Math.max(maxHeight,image.getHeight());
		}

		final BufferedImage image=new BufferedImage(sumWidth+imageList.size()-1,maxHeight+titleHeight,BufferedImage.TYPE_4BYTE_ABGR);

		final Graphics2D g=(Graphics2D)image.getGraphics();
		final int ascent=g.getFontMetrics().getAscent();
		g.setColor(Color.BLACK);
		int x=0;
		for (int i=0;i<imageList.size();i++) {
			g.drawString(titleList.get(i),x,ascent);
			g.drawImage(imageList.get(i),x,titleHeight,null);
			x+=imageList.get(i).getWidth();
		}

		try {
			ImageIO.write(image,"png",new File(folder,"all.png"));
		} catch (IOException e) {
			return false;
		}

		return true;
	}

	/**
	 * Speichert Bilder f�r alle Modellelement-Vorlagengruppen
	 * @param language	Sprache
	 * @param parentFolder	Ausgabezeichnis (die Dateinamen werden gem�� den Gruppennamen automatisch gew�hlt)
	 * @param zoom	Zoomfaktor f�r die Darstellung der Elemente in den Bildern
	 * @param out	Ausgabestream f�r Meldungen (darf nicht <code>null</code> sein)
	 * @return	Gibt an, ob die Bilder erfolgreich erstellt werden konnten
	 */
	public static boolean saveGroupImages(final String language, final File parentFolder, final double zoom, final PrintStream out) {
		final File folder=new File(parentFolder,language);
		if (!folder.isDirectory()) {
			if (!folder.mkdirs()) {
				out.println("error mkdir "+folder.toString());
				return false;
			}
		}

		final SetupData setup=SetupData.getSetup();
		final String saveLanguage=setup.language;
		setup.setLanguage(language);

		try {
			final Map<String,BufferedImage> images=getGroupImages(zoom);
			final List<String> titleList=new ArrayList<>();
			final List<BufferedImage> imageList=new ArrayList<>();

			for (int i=0;i<ModelElementCatalog.GROUP_ORDER.length;i++) {
				final String name=ModelElementCatalog.GROUP_ORDER[i];
				final File file=getImageFileName(folder,i+1,name);
				final BufferedImage image=images.get(name);

				try {
					ImageIO.write(image,"png",file);
				} catch (IOException e) {
					return false;
				}

				titleList.add(name);
				imageList.add(image);
			}

			if (!saveAllStationsImage(folder,titleList,imageList)) return false;
		} finally {
			setup.setLanguage(saveLanguage);
		}
		return true;
	}

	private static BufferedImage scaleImage(BufferedImage source, final double scale) {
		if (source==null || scale<=0) return source;
		final BufferedImage destination=new BufferedImage((int)Math.round(source.getWidth()*scale),(int)Math.round(source.getHeight()*scale),source.getType());
		destination.createGraphics().drawRenderedImage(source,AffineTransform.getScaleInstance(scale,scale));
		return destination;
	}

	private String outputImage(final File folder, final StringBuilder text, final String name, final ModelElementPosition element) {
		final BufferedImage image=scaleImage(getImage(element,2.0),2.0);
		String refName=element.getHelpPageName();
		if (refName==null) refName=element.getClass().getSimpleName();
		refName="image"+refName;
		final String fileName=refName+".png";
		final File file=new File(folder,fileName);

		try {
			ImageIO.write(image,"png",file);
		} catch (IOException e) {
			return "Error writing "+file.toString();
		}

		/*
		text.append("\\begin{figure}[ht]\n");
		text.append("\\caption{"+name+"}\n");
		text.append("\\centerline{\\includegraphics[width=2.5cm]{"+fileName+"}}\n");
		text.append("\\label{fig:"+refName+"}\n");
		text.append("\\end{figure}\n");
		 */

		text.append("\\begin{wrapfigure}{l}{2.5cm}\n");
		/* text.append("\\caption{"+name+"}\n"); */
		text.append("\\vspace{-22pt}\n");
		text.append("\\includegraphics[width=2cm]{"+fileName+"}\n");
		/* text.append("\\label{fig:"+refName+"}\n"); */
		text.append("\\vspace{-22pt}\n");
		text.append("\\end{wrapfigure}\n");

		return null;
	}

	private String tryProcessLink(final String line) {
		String s=line;
		int index1;
		int index2;

		/* Zerteilen und "ref" und "rest" bestimmen */
		index1=s.toLowerCase().indexOf("<a href=\"");
		index2=s.toLowerCase().indexOf("</a>");
		if (index1<0 || index2<0) return null;

		final StringBuilder result=new StringBuilder();
		result.append(line.substring(0,index1));
		s=s.substring(index1+9);
		index2=s.indexOf("\">");
		if (index2<0) return null;
		String ref=s.substring(0,index2);
		if (ref.toLowerCase().endsWith(".html")) ref=ref.substring(0,ref.length()-5);
		s=s.substring(index2+2);

		index2=s.toLowerCase().indexOf("</a>");
		if (index2<0) return null;
		result.append(s.substring(0,index2));

		String rest="";
		if (index2+4<s.length()) rest=s.substring(index2+4);

		/* Referenz ok? */
		boolean refOk=true;
		for (String test: removeRefs) if (test.equalsIgnoreCase(ref)) {refOk=false; break;}
		if (!refOk) {
			result.append(" "+rest.trim());
			return result.toString();
		}

		/* Zusammenbauen */
		for (String test: elementString) {
			if (rest.toLowerCase().startsWith(test.toLowerCase())) {
				if (rest.length()==test.length()) rest=""; else rest=rest.substring(test.length());
				result.append(test+" ("+seePageString+" \\pageref{ref:"+ref+"}) "+rest.trim());
				return result.toString();
			}
		}
		result.append(" ("+seePageString+" \\pageref{ref:"+ref+"}) "+rest.trim());
		return result.toString();
	}

	private static String extReplace(final String line, final String oldText, final String newText) {
		final int index=line.toLowerCase().indexOf(oldText.toLowerCase());
		if (index<0) return null;

		final StringBuilder result=new StringBuilder();
		if (index>0) result.append(line.substring(0,index));
		result.append(newText);
		if (index+oldText.length()<line.length()) result.append(line.substring(index+oldText.length()));
		return result.toString();
	}

	private static String extReplace(final String line, final String oldA, final String oldB, final String newA, final String newB) {
		final int index1=line.toLowerCase().indexOf(oldA.toLowerCase());
		final int index2=line.toLowerCase().indexOf(oldB.toLowerCase());
		if (index1<0 || index2<0) return null;

		final StringBuilder result=new StringBuilder();
		if (index1>0) result.append(line.substring(0,index1));
		result.append(newA+line.substring(index1+oldA.length(),index2)+newB);
		if (index2+oldB.length()<line.length()) result.append(line.substring(index2+oldB.length()));
		return result.toString();
	}

	private String tryReplaceB(final String line) {
		return extReplace(line,"<b>","</b>","\\textbf{","}");
	}

	private String tryReplaceU(final String line) {
		final String s=extReplace(line,"<u>","</u><br>","\\textbf{","}~\\\\");
		if (s!=null) return s;
		return extReplace(line,"<u>","</u>","\\textbf{","}");
	}

	private String tryReplaceTT(final String line) {
		return extReplace(line,"<tt>","</tt>","\\texttt{","}");
	}

	private String tryReplaceH2(final String line) {
		return extReplace(line,"<h2>","</h2>","\\subsection*{","}");
	}

	private String tryReplaceH3(final String line) {
		return extReplace(line,"<h3>","</h3>","\\subsubsection*{","}");
	}

	private String tryReplaceUL(final String line) {
		String s;
		s=extReplace(line,"<ul>","\\begin{itemize}"); if (s!=null) return s;
		s=extReplace(line,"</ul>","\\end{itemize}"); if (s!=null) return s;
		s=extReplace(line,"<li>","\\item "); if (s!=null) return s;
		s=extReplace(line,"</li>"," "); if (s!=null) return s;
		return null;
	}

	private String tryReplaceSUP(final String line) {
		return extReplace(line,"<sup>","</sup>","$^{","}$");
	}

	private String tryProcessQuotationMarks(final String line) {
		if (!replaceQuotationMarks) return null;
		String s;
		s=extReplace(line," \""," ,,"); if (s!=null) return s;
		s=extReplace(line,"\" ","'' "); if (s!=null) return s;
		s=extReplace(line,"\",","'',"); if (s!=null) return s;
		s=extReplace(line,"\".","''."); if (s!=null) return s;
		s=extReplace(line,"\"-","''-"); if (s!=null) return s;
		s=extReplace(line,"\"}","''}"); if (s!=null) return s;

		return null;
	}

	private String replaceSpecialChars(final String line) {
		String result=line.replace("&amp;","\\&");
		result=result.replace("&lt;","<");
		result=result.replace("&gt;",">");
		return result;
	}

	private void processText(final String[] lines, final StringBuilder text) {
		boolean inBody=false;
		boolean lastWasEmpty=true;

		for (String line: lines) {
			if (inBody) {
				if (line.trim().equalsIgnoreCase("</body>")) return;

				/* <h1> weglassen */
				if (line.toLowerCase().contains("<h1>")) {
					continue;
				}

				/* Abs�tze durch Leerzeilen */
				if (line.trim().equalsIgnoreCase("<p>") || line.trim().equalsIgnoreCase("</p>")) {
					if (!lastWasEmpty) text.append("\n");
					lastWasEmpty=true;
					continue;
				}

				boolean done=false;
				String s=line;
				while (!done) {
					done=true;
					String sNew;
					sNew=tryReplaceB(s); if (sNew!=null) {s=sNew; done=false;}
					sNew=tryReplaceU(s); if (sNew!=null) {s=sNew; done=false;}
					sNew=tryReplaceTT(s); if (sNew!=null) {s=sNew; done=false;}
					sNew=tryReplaceH2(s); if (sNew!=null) {s=sNew; done=false;}
					sNew=tryReplaceH3(s); if (sNew!=null) {s=sNew; done=false;}
					sNew=tryProcessLink(s); if (sNew!=null) {s=sNew; done=false;}
					sNew=tryReplaceUL(s); if (sNew!=null) {s=sNew; done=false;}
					sNew=tryReplaceSUP(s); if (sNew!=null) {s=sNew; done=false;}
					sNew=tryProcessQuotationMarks(s); if (sNew!=null) {s=sNew; done=false;}
				}

				/* Normale Textzeile */
				boolean thisIsEmpty=s.trim().isEmpty();
				if (!thisIsEmpty) text.append(replaceSpecialChars(s));
				if (!thisIsEmpty || !lastWasEmpty) text.append("\n");
				lastWasEmpty=thisIsEmpty;
			} else {
				if (line.trim().equalsIgnoreCase("<body>")) inBody=true;
			}
		}
	}

	private String outputDescription(final File outputFolder, final StringBuilder text, final ModelElementPosition element) {
		if (element.getHelpPageName()==null) return null; /* Keine Hilfe f�r dieses Element */

		final String resName=helpFolder+"/"+element.getHelpPageName()+".html";
		final URL url=helpFolderRelativeClass.getResource(resName);
		if (url==null) return "Resource not found "+resName+" (path: "+helpFolderRelativeClass.getProtectionDomain().getCodeSource().getLocation().getPath()+")";

		String[] lines=null;

		try {
			final URLConnection conn=url.openConnection();
			try (InputStream input=conn.getInputStream()) {
				final int size=input.available();
				if (size==0) return "Error reading resource "+resName;
				final byte[] bytes=new byte[size];
				if (input.read(bytes)<size) return "Error reading resource "+resName;
				lines=new String(bytes).split("\\\n");
			}
		} catch (IOException e1) {
			return "Error reading resource "+resName;
		}

		processText(lines,text);

		return null;
	}

	/**
	 * Startet die Verarbeitung.
	 * @param output	tex-Ausgabedatei. Die Bilder werden in demselben Verzeichnis erstellt.
	 * @param skipEmptyPages	Elemente ohne Hilfeseite �berspringen?
	 * @return	Im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung.
	 */
	private String run(final File output, final boolean skipEmptyPages) {
		final File outputFolder=output.getParentFile();
		if (outputFolder==null) return "No output folder";
		final String outputFileName=output.getName();
		if (outputFileName.isEmpty()) return "No output file";

		final StringBuilder text=new StringBuilder();
		String error;
		boolean firstChapter=true;

		final Map<String,Map<String,ModelElementPosition>> catalog=ModelElementCatalog.getCatalog().getAll();
		for (String groupName: ModelElementCatalog.GROUP_ORDER) {

			text.append("\\chapter{"+groupName+"}\n\n");
			if (firstChapter) {
				firstChapter=false;
				text.append("\\renewcommand{\\thepage}{\\arabic{page}}\n");
				text.append("\\setcounter{page}{1}\n\n");
			}

			final Map<String,ModelElementPosition> group=catalog.get(groupName);

			final List<String> elementNames=new ArrayList<String>(group.keySet());
			elementNames.sort(String.CASE_INSENSITIVE_ORDER);

			for (final String elementName: elementNames) {
				final ModelElementPosition element=group.get(elementName);
				final String helpPage=element.getHelpPageName();
				if (helpPage==null && skipEmptyPages) continue;

				String s=elementName;
				try {
					s=new String(s.getBytes("UTF-8"));
				} catch (java.io.UnsupportedEncodingException e) {
					return "Encoder error: "+s;
				}
				text.append("\\section{"+s+"}\n");
				if (helpPage!=null) text.append("\\label{ref:"+helpPage+"}\n");
				text.append("\n");
				error=outputImage(outputFolder,text,s,element);
				if (error!=null) return error;
				text.append("\n");
				error=outputDescription(outputFolder,text,element);
				if (error!=null) return error;
				text.append("\n");
			}
			text.append("\n\n\n");
		}

		final File file=new File(outputFolder,outputFileName);
		try {
			Files.write(Paths.get(file.toURI()),text.toString().getBytes(),StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			return "Error writing "+file.toString();
		}

		return null;
	}

	/**
	 * Erstellt basierend auf den HTML-Hilfe-Seiten zu den Elementen eine LaTeX-Dokumentation
	 * f�r eine bestimmte Sprache.
	 * @param language	Sprache
	 * @param folder	Ausgabeordner
	 * @param out	Ausgabestream f�r Meldungen (darf nicht <code>null</code> sein)
	 */
	public static void buildLanguage(final String language, final String folder, final PrintStream out) {
		if (!new File(folder).isDirectory()) {
			if (!new File(folder).mkdirs()) {
				out.println("error mkdir "+folder);
				return;
			}
		}

		final SetupData setup=SetupData.getSetup();
		final String saveLanguage=setup.language;
		setup.setLanguage(language);

		try {
			final String[] removeRefs=new String[]{"JS","Java","EditorModelDialog","PathEditorDialog","ModelElementEdge"};

			ModelElementCatalogDescriptionBuilder builder=null;
			if (language.equalsIgnoreCase("de")) {
				builder=new ModelElementCatalogDescriptionBuilder(Help.class,"pages_"+Language.tr("Numbers.Language"),"siehe Seite",new String[]{"-Elementen","-Elemente","-Element"," Elementen"},removeRefs,true);
			}
			if (builder==null) {
				builder=new ModelElementCatalogDescriptionBuilder(Help.class,"pages_"+Language.tr("Numbers.Language"),"see page",new String[]{" elements"," element"},removeRefs,false);
			}

			String result=builder.run(new File(folder,"Reference.tex"),true);
			if (result==null) result="ok";
			out.println(language+": "+result);
		} finally {
			setup.setLanguage(saveLanguage);
		}
	}

	/**
	 * Erstellt basierend auf den HTML-Hilfe-Seiten zu den Elementen eine LaTeX-Dokumentation
	 * auf Deutsch und auf Englisch in den Unterordnern "ReferenzDE" und "ReferenzEN" des Desktop-Ordners.
	 * @param out	Ausgabestream f�r Meldungen (darf nicht <code>null</code> sein)
	 */
	public static void buildAll(final PrintStream out) {
		out.println("building...");
		buildLanguage("de",System.getProperty("user.home")+"\\Desktop\\ReferenzDE",out);
		buildLanguage("en",System.getProperty("user.home")+"\\Desktop\\ReferenzEN",out);
		out.println("done.");
	}

	/**
	 * Eigene main-Routine f�r den Aufruf dieser Klasse �ber einen Ant-Task.
	 * @param args	Es werden zwei Parameter erwartet: Die Sprach-ID ("de" oder "en") und der Pfad f�r die Ausgabe.
	 */
	public static void main(String[] args) {
		if (args.length!=2) {
			System.out.println("Needing 2 parameters.");
			return;
		}
		if (!Language.isSupportedLanguage(args[0])) {
			System.out.println("Language "+args[0]+" not supported");
			return;
		}
		if (!new File(args[1]).isDirectory()) {
			System.out.println(args[1]+" is no directory");
			return;
		}

		buildLanguage(args[0].toLowerCase(),args[1],System.out);
	}
}