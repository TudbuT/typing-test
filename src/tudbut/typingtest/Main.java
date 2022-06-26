package tudbut.typingtest;

import de.tudbut.tools.Keyboard;
import de.tudbut.ui.windowgui.FontRenderer;
import de.tudbut.ui.windowgui.RenderableWindow;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Date;

public class Main {

    static RenderableWindow.RenderRunnable renderRunnable = (adaptedGraphics, graphics, bufferedImage) -> { };
    static RenderableWindow window;
    static FontRenderer renderer;
    
    public static void main(String[] args) throws InterruptedException {
    
        window = new RenderableWindow(16 * 50, 9 * 50, "WPM Typing Test Java (tudbut.wpm 0.2j)", 50, false);
        
        window.getWindow().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        window.render((adaptedGraphics, graphics, bufferedImage) -> {
            renderer = new FontRenderer(15);
        
            renderRunnable.render(adaptedGraphics, graphics, bufferedImage);
        });
        window.prepareRender();
        window.doRender();
        window.swapBuffers();
        new Thread(Main::listenForKeyPresses).start();
        while (true) {
            window.doRender();
            window.swapBuffers();
            Thread.sleep(100);
        }
    }
    
    public static void listenForKeyPresses() {
        ArrayList<String> words = new ArrayList<>();
        final ArrayList<Character> word = new ArrayList<>();
        final long[] lastChar = {new Date().getTime()};
        final long[] started = {0};
        final boolean[] stop = {false};
        window.getWindow().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
                
                if(stop[0])
                    return;
                
                if(keyEvent.getKeyChar() == ' ' || keyEvent.getKeyChar() == '\n') {
                    StringBuilder theWord = new StringBuilder();
    
                    for (int i = 0; i < word.size(); i++) {
                        if("0123456789abcdefghijklmnopqrstuvwxyz?!.*'#\"§$/-_,:;".contains(String.valueOf(word.get(i)).toLowerCase()))
                            theWord.append(word.get(i));
                    }
                    words.add(theWord.toString());
                    word.clear();
                }
                else {
                    if(keyEvent.getKeyChar() == '\u0008' && word.size() > 0)
                        word.remove(word.size() - 1);
                    else
                        word.add(keyEvent.getKeyChar());
                }
                lastChar[0] = new Date().getTime();
                if(started[0] == 0)
                    started[0] = new Date().getTime();
            }
    
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if(keyEvent.getKeyCode() == KeyEvent.VK_ALT) {
                    stop[0] = true;
                }
            }
    
            @Override
            public void keyReleased(KeyEvent keyEvent) {
        
            }
        });
        
        final long[] theLastChar = {0};
        
        while (true) {
            renderRunnable = (adaptedGraphics, graphics, bufferedImage) -> {
                if(!stop[0])
                    theLastChar[0] = new Date().getTime() - lastChar[0];
            
                adaptedGraphics.drawImage(10, 25, renderer.renderText(
                        
                        (stop[0] ? "Stopped!\n" : "Running! Press [Alt] to stop\n") +
                        "Last char typed " + theLastChar[0] + "ms ago\n" +
                        "Minutes passed = " + (Math.round(Math.max(1, lastChar[0] - started[0]) / 1000d / 60d * 100d) / 100d) + "\n" +
                        "WPM = " + (words.size() / (Math.max(1, lastChar[0] - started[0]) / 1000d / 60d)),
                        
                        0xff000000
                ));
                
                int x = 50;
                int y = 150;
                for (int i = 0; i < words.size(); i++) {
                    int w = renderer.getTextWidth(words.get(i) + " ");
                    if(x + w > window.getSizeOnScreen().getX() - 100) {
                        y += 20;
                        x = 50;
                    }
                    try {
                        adaptedGraphics.drawImage(x, y, renderer.renderText(words.get(i), 0xff000000));
                        x += w;
                    } catch (Exception ignore) { }
                }
                char[] wordString = new char[word.size()];
                for(int i = 0; i < wordString.length; i++) {
                    wordString[i] = word.get(i);
                }
                adaptedGraphics.drawImage(x, y + 20, renderer.renderText(new String(wordString), 0xff808000));
            };
        }
    }
}
