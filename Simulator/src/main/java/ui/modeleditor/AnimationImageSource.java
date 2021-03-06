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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.commons.math3.util.FastMath;

/**
 * Diese Klasse h�lt Bilder f�r die Animation von Modellen vor.
 * Die Bilder k�nnen dabei direkt in einer passend skalierten Gr��e ausgeliefert werden.
 * @author Alexander Herzog
 */
public class AnimationImageSource {
	/* Famfamfam Icons */

	/** Name f�r Animationssymbol "B�roklammer" */
	public static String iconNameAttach="B�roklammer";
	/** Name f�r Animationssymbol "Buch" */
	public static String iconNameBook="Buch";
	/** Name f�r Animationssymbol "K�fer" */
	public static String iconNameBug="K�fer";
	/** Name f�r Animationssymbol "CD" */
	public static String iconNameCD="CD";
	/** Name f�r Animationssymbol "Kuchen" */
	public static String iconNameCake="Kuchen";
	/** Name f�r Animationssymbol "Auto" */
	public static String iconNameCar="Auto";
	/** Name f�r Animationssymbol "Einkaufswagen" */
	public static String iconNameCart="Einkaufswagen";
	/** Name f�r Animationssymbol "Uhr" */
	public static String iconNameClock="Uhr";
	/** Name f�r Animationssymbol "Computer" */
	public static String iconNameComputer="Computer";
	/** Name f�r Animationssymbol "Datenbank" */
	public static String iconNameDatabase="Datenbank";
	/** Name f�r Animationssymbol "Diskette" */
	public static String iconNameDisk="Diskette";
	/** Name f�r Animationssymbol "Brief" */
	public static String iconNameLetter="Brief";
	/** Name f�r Animationssymbol "Smiley" */
	public static String iconNameSmiley="Smiley";
	/** Name f�r Animationssymbol "Symbol - Frau" */
	public static String iconNameSymbolFemale="Symbol - Frau";
	/** Name f�r Animationssymbol "Symbol - Mann" */
	public static String iconNameSymbolMale="Symbol - Mann";
	/** Name f�r Animationssymbol "Fahne - Blue" */
	public static String iconNameFlagBlue="Fahne - Blue";
	/** Name f�r Animationssymbol "Fahne - Gr�n" */
	public static String iconNameFlagGreen="Fahne - Gr�n";
	/** Name f�r Animationssymbol "Fahne - Orange" */
	public static String iconNameFlagOrange="Fahne - Orange";
	/** Name f�r Animationssymbol "Fahne - Pink" */
	public static String iconNameFlagPink="Fahne - Pink";
	/** Name f�r Animationssymbol "Fahne - Lila" */
	public static String iconNameFlagPurple="Fahne - Lila";
	/** Name f�r Animationssymbol "Fahne - Rot" */
	public static String iconNameFlagRed="Fahne - Rot";
	/** Name f�r Animationssymbol "Fahne - Gelb" */
	public static String iconNameFlagYellow="Fahne - Gelb";
	/** Name f�r Animationssymbol "Herz" */
	public static String iconNameHeart="Herz";
	/** Name f�r Animationssymbol "Schl�ssel" */
	public static String iconNameKey="Schl�ssel";
	/** Name f�r Animationssymbol "LKW" */
	public static String iconNameLorry="LKW";
	/** Name f�r Animationssymbol "LKW-links" */
	public static String iconNameLorryLeft="LKW-links";
	/** Name f�r Animationssymbol "LKW (leer)" */
	public static String iconNameLorryEmpty="LKW (leer)";
	/** Name f�r Animationssymbol "LKW-links (leer)" */
	public static String iconNameLorryLeftEmpty="LKW-links (leer)";
	/** Name f�r Animationssymbol "Geld - Dollar" */
	public static String iconNameMoneyDollar="Geld - Dollar";
	/** Name f�r Animationssymbol "Geld - Euro" */
	public static String iconNameMoneyEuro="Geld - Euro";
	/** Name f�r Animationssymbol "Geld - Pfund" */
	public static String iconNameMoneyPound="Geld - Pfund";
	/** Name f�r Animationssymbol "Geld - Yen" */
	public static String iconNameMoneyYen="Geld - Yen";
	/** Name f�r Animationssymbol "Musik" */
	public static String iconNameMusic="Musik";
	/** Name f�r Animationssymbol "Dokument" */
	public static String iconNameDocument="Dokument";
	/** Name f�r Animationssymbol "Stern" */
	public static String iconNameStar="Stern";
	/** Name f�r Animationssymbol "Person - Grau" */
	public static String iconNamePersonGray="Person - Grau";
	/** Name f�r Animationssymbol "Person - Gr�n" */
	public static String iconNamePersonGreen="Person - Gr�n";
	/** Name f�r Animationssymbol "Person - Orange" */
	public static String iconNamePersonOrange="Person - Orange";
	/** Name f�r Animationssymbol "Person - Rot" */
	public static String iconNamePersonRed="Person - Rot";
	/** Name f�r Animationssymbol "Person - Anzug" */
	public static String iconNamePersonSuit="Person - Anzug";
	/** Name f�r Animationssymbol "Person - Blau" */
	public static String iconNamePersonBlue="Person - Blau";
	/** Name f�r Animationssymbol "Notiz" */
	public static String iconNameNote="Notiz";
	/** Name f�r Animationssymbol "Paket" */
	public static String iconNamePackage="Paket";
	/** Name f�r Animationssymbol "Wolken" */
	public static String iconNameClouds="Wolken";
	/** Name f�r Animationssymbol "Gewitter" */
	public static String iconNameLightning="Gewitter";
	/** Name f�r Animationssymbol "Sonne" */
	public static String iconNameSun="Sonne";
	/** Name f�r Animationssymbol "Welt" */
	public static String iconNameWorld="Welt";
	/** Name f�r Animationssymbol "Kreis - Rot" */
	public static String iconNameBallRed="Kreis - Rot";
	/** Name f�r Animationssymbol "Kreis - Blau" */
	public static String iconNameBallBlue="Kreis - Blau";
	/** Name f�r Animationssymbol "Kreis - Gelb" */
	public static String iconNameBallYellow="Kreis - Gelb";
	/** Name f�r Animationssymbol "Kreis - Gr�n" */
	public static String iconNameBallGreen="Kreis - Gr�n";
	/** Name f�r Animationssymbol "Kreis - Schwarz" */
	public static String iconNameBallBlack="Kreis - Schwarz";
	/** Name f�r Animationssymbol "Kreis - Wei�" */
	public static String iconNameBallWhite="Kreis - Wei�";
	/** Name f�r Animationssymbol "Kreis - Orange" */
	public static String iconNameBallOrange="Kreis - Orange";
	/** Name f�r Animationssymbol "Kreis - Grau" */
	public static String iconNameBallGray="Kreis - Grau";
	/** Name f�r Animationssymbol "Zahnrad" */
	public static String iconNameCog="Zahnrad";
	/** Name f�r Animationssymbol "Auge" */
	public static String iconNameEye="Auge";
	/** Name f�r Animationssymbol "Haus" */
	public static String iconNameHouse="Haus";
	/** Name f�r Animationssymbol "Bausteine" */
	public static String iconNameBricks="Bausteine";
	/** Name f�r Animationssymbol "Bediener" */
	public static String iconNameOperator="Bediener";
	/** Name f�r Animationssymbol "Farbe - Rot" */
	public static String iconNameColorRed="Farbe - Rot";
	/** Name f�r Animationssymbol "Farbe - Blau" */
	public static String iconNameColorBlue="Farbe - Blau";
	/** Name f�r Animationssymbol "Farbe - Gelb" */
	public static String iconNameColorYellow="Farbe - Gelb";
	/** Name f�r Animationssymbol "Farbe - Gr�n" */
	public static String iconNameColorGreen="Farbe - Gr�n";
	/** Name f�r Animationssymbol "Farbe - Schwarz" */
	public static String iconNameColorBlack="Farbe - Schwarz";
	/** Name f�r Animationssymbol "Farbe - Wei�" */
	public static String iconNameColorWhite="Farbe - Wei�";
	/** Name f�r Animationssymbol "Farbe - Orange" */
	public static String iconNameColorOrange="Farbe - Orange";
	/** Name f�r Animationssymbol "Farbe - Grau" */
	public static String iconNameColorGray="Farbe - Grau";
	/** Name f�r Animationssymbol "Pfeil - nach unten" */
	public static String iconNameArrowDown="Pfeil - nach unten";
	/** Name f�r Animationssymbol "Pfeil - nach links" */
	public static String iconNameArrowLeft="Pfeil - nach links";
	/** Name f�r Animationssymbol "Pfeil - nach rechts" */
	public static String iconNameArrowRight="Pfeil - nach rechts";
	/** Name f�r Animationssymbol "Pfeil - nach oben" */
	public static String iconNameArrowUp="Pfeil - nach oben";
	/** Name f�r Animationssymbol "Fragezeichen" */
	public static String iconNameQuestionmark="Fragezeichen";

	/* p.yusukekamiyamane Icons */

	/** Name f�r Animationssymbol "Hund" */
	public static String iconNameAnimalDog="Hund";
	/** Name f�r Animationssymbol "Affe" */
	public static String iconNameAnimalMonkey="Affe";
	/** Name f�r Animationssymbol "Pinguin" */
	public static String iconNameAnimalPenguin="Pinguin";
	/** Name f�r Animationssymbol "Katze" */
	public static String iconNameAnimalCat="Katze";
	/** Name f�r Animationssymbol "Batterie" */
	public static String iconNameBattery="Batterie";
	/** Name f�r Animationssymbol "Glocke" */
	public static String iconNameBell="Glocke";
	/** Name f�r Animationssymbol "Aktentasche" */
	public static String iconNameBriefcase="Aktentasche";
	/** Name f�r Animationssymbol "Kerze" */
	public static String iconNameCandle="Kerze";
	/** Name f�r Animationssymbol "Taxi" */
	public static String iconNameTaxi="Taxi";
	/** Name f�r Animationssymbol "Glaskolben" */
	public static String iconNameFlask="Glaskolben";
	/** Name f�r Animationssymbol "Blume" */
	public static String iconNameFlower="Blume";
	/** Name f�r Animationssymbol "Hamburger" */
	public static String iconNameHamburger="Hamburger";
	/** Name f�r Animationssymbol "Hand" */
	public static String iconNameHand="Hand";
	/** Name f�r Animationssymbol "Einmachglas" */
	public static String iconNameJar="Einmachglas";
	/** Name f�r Animationssymbol "Laubblatt" */
	public static String iconNameLeaf="Laubblatt";
	/** Name f�r Animationssymbol "Rettungsring" */
	public static String iconNameLifebuoy="Rettungsring";
	/** Name f�r Animationssymbol "Gl�hbirne-aus" */
	public static String iconNameLightbulbOff="Gl�hbirne-aus";
	/** Name f�r Animationssymbol "Gl�hbirne-an" */
	public static String iconNameLightbuldOn="Gl�hbirne-an";
	/** Name f�r Animationssymbol "Lutscher" */
	public static String iconNameLollipop="Lutscher";
	/** Name f�r Animationssymbol "Farbpalette" */
	public static String iconNamePalette="Farbpalette";
	/** Name f�r Animationssymbol "Stecknadel" */
	public static String iconNamePin="Stecknadel";
	/** Name f�r Animationssymbol "Fu�ball" */
	public static String iconNameSoccer="Fu�ball";
	/** Name f�r Animationssymbol "Werkzeugkasten" */
	public static String iconNameToolbox="Werkzeugkasten";
	/** Name f�r Animationssymbol "Pylon" */
	public static String iconNameTrafficCone="Pylon";
	/** Name f�r Animationssymbol "Regenschirm" */
	public static String iconNameUmbrella="Regenschirm";
	/** Name f�r Animationssymbol "Wassertropfen" */
	public static String iconNameWaterDrop="Wassertropfen";

	/**
	 * Zuordnung von Animationssymbol-Namen zu Dateinamen f�r die Symbole
	 */
	public static Map<String,String> ICONS;

	/**
	 * Initialisiert {@link #ICONS}. Dies erfolgt beim Laden
	 * der Klasse automatisch und muss nur nach einem Wechsel
	 * der Sprache manuell angesto�en werden.
	 */
	public static void initIconsMap() {
		ICONS=new HashMap<>();

		/* Famfamfam Icons */

		ICONS.put(iconNameAttach,"attach");
		ICONS.put(iconNameBook,"book");
		ICONS.put(iconNameBug,"bug");
		ICONS.put(iconNameCD,"cd");
		ICONS.put(iconNameCake,"cake");
		ICONS.put(iconNameCar,"car");
		ICONS.put(iconNameCart,"cart");
		ICONS.put(iconNameClock,"clock");
		ICONS.put(iconNameComputer,"computer");
		ICONS.put(iconNameDatabase,"database");
		ICONS.put(iconNameDisk,"disk");
		ICONS.put(iconNameLetter,"email");
		ICONS.put(iconNameSmiley,"emoticon_smile");
		ICONS.put(iconNameSymbolFemale,"female");
		ICONS.put(iconNameFlagBlue,"flag_blue");
		ICONS.put(iconNameFlagGreen,"flag_green");
		ICONS.put(iconNameFlagOrange,"flag_orange");
		ICONS.put(iconNameFlagPink,"flag_pink");
		ICONS.put(iconNameFlagPurple,"flag_purple");
		ICONS.put(iconNameFlagRed,"flag_red");
		ICONS.put(iconNameFlagYellow,"flag_yellow");
		ICONS.put(iconNameHeart,"heart");
		ICONS.put(iconNameKey,"key");
		ICONS.put(iconNameLorry,"lorry");
		ICONS.put(iconNameLorryLeft,"lorry-left");
		ICONS.put(iconNameLorryEmpty,"lorry-empty");
		ICONS.put(iconNameLorryLeftEmpty,"lorry-left-empty");
		ICONS.put(iconNameSymbolMale,"male");
		ICONS.put(iconNameMoneyDollar,"money_dollar");
		ICONS.put(iconNameMoneyEuro,"money_euro");
		ICONS.put(iconNameMoneyPound,"money_pound");
		ICONS.put(iconNameMoneyYen,"money_yen");
		ICONS.put(iconNameMusic,"music");
		ICONS.put(iconNameDocument,"page_white");
		ICONS.put(iconNameStar,"star");
		ICONS.put(iconNamePersonGray,"user_gray");
		ICONS.put(iconNamePersonGreen,"user_green");
		ICONS.put(iconNamePersonOrange,"user_orange");
		ICONS.put(iconNamePersonRed,"user_red");
		ICONS.put(iconNamePersonSuit,"user_suit");
		ICONS.put(iconNamePersonBlue,"user");
		ICONS.put(iconNameNote,"note");
		ICONS.put(iconNamePackage,"icon_package");
		ICONS.put(iconNameClouds,"weather_cloudy");
		ICONS.put(iconNameLightning,"weather_lightning");
		ICONS.put(iconNameSun,"weather_sun");
		ICONS.put(iconNameWorld,"world");
		ICONS.put(iconNameBallRed,"Ball_red");
		ICONS.put(iconNameBallBlue,"Ball_blue");
		ICONS.put(iconNameBallYellow,"Ball_yellow");
		ICONS.put(iconNameBallGreen,"Ball_green");
		ICONS.put(iconNameBallBlack,"Ball_black");
		ICONS.put(iconNameBallWhite,"Ball_white");
		ICONS.put(iconNameBallOrange,"Ball_orange");
		ICONS.put(iconNameBallGray,"Ball_gray");
		ICONS.put(iconNameCog,"cog");
		ICONS.put(iconNameEye,"eye");
		ICONS.put(iconNameHouse,"house");
		ICONS.put(iconNameBricks,"bricks");
		ICONS.put(iconNameOperator,"status_online");
		ICONS.put(iconNameColorRed,"Intern-1");
		ICONS.put(iconNameColorBlue,"Intern-2");
		ICONS.put(iconNameColorYellow,"Intern-3");
		ICONS.put(iconNameColorGreen,"Intern-4");
		ICONS.put(iconNameColorBlack,"Intern-5");
		ICONS.put(iconNameColorWhite,"Intern-6");
		ICONS.put(iconNameColorOrange,"Intern-7");
		ICONS.put(iconNameColorGray,"Intern-8");
		ICONS.put(iconNameArrowDown,"arrow_down");
		ICONS.put(iconNameArrowLeft,"arrow_left");
		ICONS.put(iconNameArrowRight,"arrow_right");
		ICONS.put(iconNameArrowUp,"arrow_up");
		ICONS.put(iconNameQuestionmark,"questionmark");

		/* p.yusukekamiyamane Icons */

		ICONS.put(iconNameAnimalDog,"animal-dog");
		ICONS.put(iconNameAnimalMonkey,"animal-monkey");
		ICONS.put(iconNameAnimalPenguin,"animal-penguin");
		ICONS.put(iconNameAnimalCat,"animal");
		ICONS.put(iconNameBattery,"battery");
		ICONS.put(iconNameBell,"bell");
		ICONS.put(iconNameBriefcase,"briefcase");
		ICONS.put(iconNameCandle,"candle");
		ICONS.put(iconNameTaxi,"car-taxi");
		ICONS.put(iconNameFlask,"flask");
		ICONS.put(iconNameFlower,"flower");
		ICONS.put(iconNameHamburger,"hamburger");
		ICONS.put(iconNameHand,"hand");
		ICONS.put(iconNameHeart,"heart");
		ICONS.put(iconNameJar,"jar");
		ICONS.put(iconNameLeaf,"leaf");
		ICONS.put(iconNameLifebuoy,"lifebuoy");
		ICONS.put(iconNameLightbulbOff,"light-bulb-off");
		ICONS.put(iconNameLightbuldOn,"light-bulb");
		ICONS.put(iconNameLollipop,"lollipop");
		ICONS.put(iconNamePalette,"palette");
		ICONS.put(iconNamePin,"pin");
		ICONS.put(iconNameSoccer,"sport-soccer");
		ICONS.put(iconNameToolbox,"toolbox");
		ICONS.put(iconNameTrafficCone,"traffic-cone");
		ICONS.put(iconNameUmbrella,"umbrella");
		ICONS.put(iconNameWaterDrop,"water");
	}

	static {
		initIconsMap();
	}

	private final static String IMAGE_PATH="animation/";

	private Map<Long,BufferedImage> cache;

	/**
	 * Konstruktor der Klasse <code>AnimationImageSource</code>
	 */
	public AnimationImageSource() {
		cache=new HashMap<>();
	}

	/**
	 * L�scht alle Bilder aus dem Cache.<br>
	 * (Ist n�tig, wenn die Liste der benutzerdefinierten Bilder w�hrend der Verwendung dieses Objekts ver�ndert wurde.)
	 */
	public void clearCache() {
		cache.clear();
	}

	private BufferedImage getEmptyImage(int size) {
		if (size<=0) size=32;
		if (size>=2048) size=2048;

		final BufferedImage image=new BufferedImage(size,size,BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics g=image.getGraphics();
		g.setColor(Color.RED);
		g.drawRect(0,0,size-1,size-1);
		g.drawLine(0,0,size-1,size-1);
		g.drawLine(size-1,0,0,size-1);
		return image;
	}

	/**
	 * Skaliert ein Bild auf eine vorgegebene Breite
	 * @param original	Ausgangsbild
	 * @param width	Zielbreite
	 * @return	Neues Bild; wenn das Ausgangsbild bereits die korrekte Breit hat, wird das Ausgangsbild zur�ckgeliefert.
	 */
	public static BufferedImage scaleImage(final BufferedImage original, final int width) {
		if (original.getWidth()==width && original.getHeight()==width) return original;
		final Image image=original.getScaledInstance(width,(int)Math.round(((double)original.getHeight())/original.getWidth()*width),Image.SCALE_SMOOTH);
		final BufferedImage bufferedImage=new BufferedImage(image.getWidth(null),image.getHeight(null),BufferedImage.TYPE_4BYTE_ABGR);
		bufferedImage.getGraphics().drawImage(image,0,0,null);
		return bufferedImage;
	}

	private BufferedImage getInternalImage(final int index, int preferredSize) {
		if (preferredSize<=0) preferredSize=128;
		if (preferredSize>=2048) preferredSize=2048;
		final BufferedImage image=new BufferedImage(preferredSize,preferredSize,BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics g=image.getGraphics();
		switch (index) {
		case 1:
			g.setColor(Color.RED);
			g.fillRect(0,0,preferredSize,preferredSize);
			break;
		case 2:
			g.setColor(Color.BLUE);
			g.fillRect(0,0,preferredSize,preferredSize);
			break;
		case 3:
			g.setColor(Color.YELLOW);
			g.fillRect(0,0,preferredSize,preferredSize);
			break;
		case 4:
			g.setColor(Color.GREEN);
			g.fillRect(0,0,preferredSize,preferredSize);
			break;
		case 5:
			g.setColor(Color.BLACK);
			g.fillRect(0,0,preferredSize,preferredSize);
			break;
		case 6:
			g.setColor(Color.WHITE);
			g.fillRect(0,0,preferredSize,preferredSize);
			break;
		case 7:
			g.setColor(Color.ORANGE);
			g.fillRect(0,0,preferredSize,preferredSize);
			break;
		case 8:
			g.setColor(Color.GRAY);
			g.fillRect(0,0,preferredSize,preferredSize);
			break;
		}

		return image;
	}

	private static Map<String,BufferedImage> resourceCache=new HashMap<>();

	private synchronized BufferedImage loadImageFromResource(final String name, final ModelAnimationImages modelImages, final int preferredSize) {
		/* Bilder aus Modell laden */
		if (modelImages!=null) {
			final BufferedImage image=modelImages.getLocal(name);
			if (image!=null) return image;
		}

		/* Per Zeichenbefehle generierte Bilder */
		final String INTERN="Intern-";
		if (name.startsWith(INTERN)) {
			try {
				final int index=Integer.parseInt(name.substring(INTERN.length()));
				return getInternalImage(index,preferredSize);
			} catch (NumberFormatException e) {} /* weiter unten */
		}

		/* Bilder aus Ressourcen laden */
		final BufferedImage cacheImage=resourceCache.get(name);
		if (cacheImage!=null) return cacheImage;
		final URL url=getClass().getResource(IMAGE_PATH+name+".png");
		if (url==null) return getEmptyImage(preferredSize);
		try {
			final BufferedImage image=ImageIO.read(url);
			resourceCache.put(name,image);
			return image;
		} catch (IOException e) {
			final BufferedImage image=getEmptyImage(preferredSize);
			resourceCache.put(name,image);
			return image;
		}
	}

	/**
	 * Liefert ein Bild in einer bestimmten Gr��e aus
	 * @param name	Name des Bildes (ohne Pfad und ohne Dateiendung)
	 * @param modelImages	Benutzerdefinierte Animationsicons
	 * @param size	Gew�nschte Breite des Bildes
	 * @return	Bild in der angegebenen Gr��e (existiert das Bild nicht, wird ein Fehler-Bild geliefert, d.h. es wird immer ein Bild geliefert)
	 */
	public BufferedImage get(final String name, final ModelAnimationImages modelImages, final int size) {
		final long key=name.hashCode()*1000L+size;
		/* verbraucht mehr Speicher: final String key=name+"-"+size; */
		BufferedImage image=cache.get(key);
		if (image!=null) return image;

		image=loadImageFromResource(name,modelImages,size);
		image=scaleImage(image,size);
		cache.put(key,image);
		return image;
	}

	/**
	 * Liefert das Bild in einer gegen�ber der nativen Gr��e skalierten Fassung
	 * @param name	Name des Bildes (ohne Pfad und ohne Dateiendung)
	 * @param modelImages	Benutzerdefinierte Animationsicons
	 * @param zoom	Zoomfaktor
	 * @return	Bild in der angegebenen Gr��e (existiert das Bild nicht, wird ein Fehler-Bild geliefert, d.h. es wird immer ein Bild geliefert)
	 */
	public BufferedImage getZoomed(final String name, final ModelAnimationImages modelImages, final double zoom) {
		final int preferredSize=FastMath.max(4,(int)Math.round(16*zoom));
		final BufferedImage image=loadImageFromResource(name,modelImages,preferredSize);
		return scaleImage(image,preferredSize);
	}

	/**
	 * Liefert die Breite des Bildes im Normalfall
	 * @param name	Name des Bildes (ohne Pfad und ohne Dateiendung)
	 * @param modelImages	Benutzerdefinierte Animationsicons
	 * @return	Breite und H�he des Bildes
	 */
	public int[] getNativeSize(final String name, final ModelAnimationImages modelImages) {
		final BufferedImage image=loadImageFromResource(name,modelImages,-1);
		return new int[] {image.getWidth(),image.getHeight()};
	}

	/**
	 * Liefert ein Bild in der maximal zur Verf�gung stehenden Gr��e aus
	 * @param name	Name des Bildes (ohne Pfad und ohne Dateiendung)
	 * @param modelImages	Benutzerdefinierte Animationsicons
	 * @return	Bild in der maximal m�glichen Gr��e (existiert das Bild nicht, wird ein Fehler-Bild geliefert, d.h. es wird immer ein Bild geliefert)
	 */
	public BufferedImage getMaxSize(final String name, final ModelAnimationImages modelImages) {
		return loadImageFromResource(name,modelImages,-1);
	}

	/**
	 * Liefert ein Bild in einer bestimmten Gr��e aus
	 * @param name	Name des Bildes (ohne Pfad und ohne Dateiendung)
	 * @param modelImages	Benutzerdefinierte Animationsicons
	 * @param baseSize	Gew�nschte Breite des Bildes bezogen auf einen Zoomfaktor von 100%
	 * @param zoom	Zoomfaktor
	 * @return	Bild in der angegebenen Gr��e (existiert das Bild nicht, wird ein Fehler-Bild geliefert, d.h. es wird immer ein Bild geliefert)
	 */
	public BufferedImage get(final String name, final ModelAnimationImages modelImages, final int baseSize, final double zoom) {
		return get(name,modelImages,(int)FastMath.round(baseSize*zoom));
	}

	private List<String> getSortedIconsList(final ModelAnimationImages modelImages) {
		final List<String> names=new ArrayList<>(ICONS.keySet());
		if (modelImages!=null) names.addAll(Arrays.asList(modelImages.getLocalNames()));
		names.sort(null);
		return names;
	}

	/**
	 * Liefert eine Liste mit allen verf�gbaren Icons f�r eine Combobox
	 * @param modelImages	Benutzerdefinierte Animationsicons
	 * @return	ComboBox-Modell mit allen verf�gbaren Icons
	 */
	public DefaultComboBoxModel<JLabel> getIconsComboBox(final ModelAnimationImages modelImages) {
		final DefaultComboBoxModel<JLabel> iconsListModel=new DefaultComboBoxModel<>();

		for (String name: getSortedIconsList(modelImages)) {
			final JLabel label=new JLabel(name);
			String imageName=ICONS.get(name);
			if (imageName==null) imageName=name;
			final BufferedImage image=get(imageName,modelImages,16);
			label.setIcon(new ImageIcon(image));
			iconsListModel.addElement(label);
		}
		return iconsListModel;
	}

	/**
	 * F�gt alle verf�gbaren Icons als Men�punkte zu einem Men� hinzu
	 * @param menu	Men�, zu dem die Punkte hinzugef�gt werden sollen
	 * @param clickCallback	Callback-Funktion, die bei einem Klick auf einen der Men�punkte aufgerufen werden soll. Es wird der Dateiname (ohne Endung) des Bildes �bergeben (der direkt f�r <code>get</code> verwendet werden kann) und nicht der Anzeigename.
	 * @param modelImages	Benutzerdefinierte Animationsicons
	 */
	public void addIconsToMenu(final JMenu menu, final Consumer<String> clickCallback, final ModelAnimationImages modelImages) {
		for (String name: getSortedIconsList(modelImages)) {
			String imageName=ICONS.get(name);
			if (imageName==null) imageName=name;
			final JMenuItem item=new JMenuItem(name,new ImageIcon(get(imageName,modelImages,16)));
			final String fixName=imageName;
			item.addActionListener(e->clickCallback.accept(fixName));
			menu.add(item);
		}
	}

	/**
	 * F�gt alle verf�gbaren Icons als Men�punkte zu einem Men� hinzu
	 * @param menu	Men�, zu dem die Punkte hinzugef�gt werden sollen
	 * @param clickCallback	Callback-Funktion, die bei einem Klick auf einen der Men�punkte aufgerufen werden soll. Es wird der Dateiname (ohne Endung) des Bildes �bergeben (der direkt f�r <code>get</code> verwendet werden kann) und nicht der Anzeigename.
	 * @param modelImages	Benutzerdefinierte Animationsicons
	 */
	public void addIconsToMenu(final JPopupMenu menu, final Consumer<String> clickCallback, final ModelAnimationImages modelImages) {
		for (String name: getSortedIconsList(modelImages)) {
			final String imageName=ICONS.get(name);
			final JMenuItem item=new JMenuItem(name,new ImageIcon(get(imageName,modelImages,16)));
			item.addActionListener(e->clickCallback.accept(imageName));
			menu.add(item);
		}
	}

	/**
	 * ComboBox ListCellRenderer, der es erm�glicht, Icons inkl. Beschriftungen in der Liste anzuzeigen
	 * @author Alexander Herzog
	 */
	public static class IconComboBoxCellRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 6913560392242517601L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index,boolean isSelected, boolean cellHasFocus) {
			Component renderer=super.getListCellRendererComponent(list,value, index, isSelected, cellHasFocus);
			if (value instanceof JLabel) {
				((IconComboBoxCellRenderer)renderer).setText(((JLabel)value).getText());
				((IconComboBoxCellRenderer)renderer).setIcon(((JLabel)value).getIcon());
			}
			return renderer;
		}
	}
}
