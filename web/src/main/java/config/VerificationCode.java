package config;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

/*
 * ������֤��Ĺ�����
 */
public class VerificationCode {

	private int width = 100;// ������֤��ͼƬ�Ŀ��
	private int height = 30;// ������֤��ͼƬ�ĸ߶�
	private String[] fontNames = {"����", "����", "����", "΢���ź�" };
	private Color bgColor = new Color(255, 255, 255);//������֤��ͼƬ�ı�����ɫΪ��ɫ?
	private Random random = new Random();
	private String codes = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private String text;//��¼����ַ���

	/**
	 * ��ȡһ��������ɫ
	 * 
	 * @return
	 */
	private Color randomColor() {
		int red = random.nextInt(150);
		int green = random.nextInt(150);
		int blue = random.nextInt(150);
		return new Color(red, green, blue);
	}

	/**
	 * ��ȡһ���������
	 * 
	 * @return
	 */
	private Font randomFont() {
		String name = fontNames[random.nextInt(fontNames.length)];
		int style = random.nextInt(4);
		int size = random.nextInt(5) + 24;
		return new Font(name, style, size);
	}

	/**
	 * ��ȡһ������ַ�
	 * 
	 * @return
	 */
	private char randomChar() {
		return codes.charAt(random.nextInt(codes.length()));
	}

	/**
	 * ����һ���հ׵�BufferedImage����
	 * 
	 * @return
	 */
	private BufferedImage createImage() {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = (Graphics2D) image.getGraphics();
		g2.setColor(bgColor);//������֤��ͼƬ�ı�����ɫ
		g2.fillRect(0, 0, width, height);
		return image;
	}

	public BufferedImage getImage() {
		BufferedImage image = createImage();
		Graphics2D g2 = (Graphics2D) image.getGraphics();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 4; i++) {
			String s = randomChar() + "";
			sb.append(s);
			g2.setColor(randomColor());
			g2.setFont(randomFont());
			float x = i * width * 1.0f / 4;
			g2.drawString(s, x, height - 8);
		}
		this.text = sb.toString();
		drawLine(image);
		return image;
	}

	/**
	 * ���Ƹ�����
	 * 
	 * @param image
	 */
	private void drawLine(BufferedImage image) {
		Graphics2D g2 = (Graphics2D) image.getGraphics();
		int num = 5;
		for (int i = 0; i < num; i++) {
			int x1 = random.nextInt(width);
			int y1 = random.nextInt(height);
			int x2 = random.nextInt(width);
			int y2 = random.nextInt(height);
			g2.setColor(randomColor());
			g2.setStroke(new BasicStroke(1.5f));
			g2.drawLine(x1, y1, x2, y2);
		}
	}

	public String getText() {
		return text;
	}

	public static void output(BufferedImage image, OutputStream out) throws IOException {
		ImageIO.write(image, "JPEG", out);
	}
}