package com.andrew.tetris;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static java.awt.event.KeyEvent.CHAR_UNDEFINED;

public interface JavaConsoleFacade {
    void setTitle(String title);

    /**
     * Image im = Toolkit.getDefaultToolkit().getImage("../images/MyIcon.png");
     * console.setIconImage(im);
     *
     * @param i An image
     */
    void setIconImage(Image i);

    void setFontSize(float size);

    Font getFont();

    void setFont(Font f);

    Color getForeground();

    void setForeground(Color fg);

    Color getBackground();

    void setBackground(Color bg);

    void useBlockCaret(boolean isBlockCaret);

    void setCaretVisible(boolean isVisible);

    boolean isClearButtonVisible();

    void setClearButtonVisible(boolean isVisible);

    void clear();

    /**
     * @author RJHM van den Bergh , rvdb@comweb.nl
     * @brief A simple Java Console for your application (Swing version).
     * @par Comments: Original located at
     * http://www.comweb.nl/java/Console/Console.html
     * @history 02-07-2012 David MacDermot Marked: DWM 02-07-2012 Added
     * KeyListener to pipe text to STDIN. Added custom block style caret. Added
     * various other customizations.
     * // https://www.codeproject.com/articles/328417/java-console-apps-made-easy
     * @bug
     */
    final class JavaConsole extends WindowAdapter implements WindowListener, ActionListener, JavaConsoleFacade {

        private static final int DEFAULT_FONT_SIZE = 18;
        private static final String WINDOW_HEADER_TEXT = "Java Console Emulator";

        private static JavaConsole javaConsole;
        // signals the Threads that they should exit
        private static volatile boolean quit = false;
        private final JFrame frame;
        private final JTextArea textArea;
        private final JButton button = new JButton("clear");
        private final Caret defaultCaret = new DefaultCaret();
        private final Thread reader;
        private final Thread reader2;
        private final PipedInputStream pin = new PipedInputStream();
        private final PipedInputStream pin2 = new PipedInputStream();
        private final PipedOutputStream pout3 = new PipedOutputStream();

        /**
         * @brief Class Constructor
         */
        private JavaConsole() {
            textArea = new ExJTextArea();
            textArea.setBackground(Color.black);
            textArea.setForeground(Color.white);
            textArea.setCaretColor(textArea.getForeground());
            textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, DEFAULT_FONT_SIZE));
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setEditable(true);

            button.setVisible(false);

            final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            final Dimension frameSize = new Dimension((int) (screenSize.width / 2), (int) (screenSize.height / 2));
            final int x = (int) frameSize.width / 2;
            final int y = (int) frameSize.height / 2;

            frame = new JFrame(WINDOW_HEADER_TEXT);
            frame.setBounds(x, y, frameSize.width, frameSize.height);
            frame.getContentPane().setLayout(new BorderLayout());
            frame.getContentPane().add(new JScrollPane(textArea), BorderLayout.CENTER);
            frame.getContentPane().add(button, BorderLayout.SOUTH);
            frame.setVisible(true);
            frame.addWindowListener(this);

            try {
                PipedOutputStream pout = new PipedOutputStream(this.pin);
                System.setOut(new PrintStream(pout, true));
            } catch (IOException | SecurityException io) {
                textArea.append("Couldn't redirect STDOUT to this console\n" + io.getMessage());
                textArea.setCaretPosition(textArea.getDocument().getLength());
            }

            try {
                PipedOutputStream pout2 = new PipedOutputStream(this.pin2);
                System.setErr(new PrintStream(pout2, true));
            } catch (IOException | SecurityException io) {
                textArea.append("Couldn't redirect STDERR to this console\n" + io.getMessage());
                textArea.setCaretPosition(textArea.getDocument().getLength());
            }

            try {
                System.setIn(new PipedInputStream(this.pout3));
            } catch (IOException | SecurityException io) {
                textArea.append("Couldn't redirect STDIN to this console\n" + io.getMessage());
                textArea.setCaretPosition(textArea.getDocument().getLength());
            }

            textArea.addKeyListener(new KeyListener() {
                private final Document document;

                {
                    document = textArea.getDocument();
                }

                @Override
                public void keyPressed(KeyEvent e) {
                    try {
                        // consume a backspace when line is empty
                        if (KeyEvent.VK_BACK_SPACE == e.getKeyChar() && document.getLength() > 0) {
                            final String lastChar = document.getText(document.getLength() - 1, 1);
                            if (KeyEvent.VK_ENTER == lastChar.charAt(0)) {
                                e.consume();
                            }
                        }
                    } catch (BadLocationException ignored) {
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                }

                @Override
                public void keyTyped(KeyEvent e) {
                    try {
                        char keyChar = e.getKeyChar();
                        if (KeyEvent.VK_ENTER == keyChar || (!Character.isISOControl(keyChar) && CHAR_UNDEFINED != keyChar)) {
                            pout3.write(String.valueOf(keyChar).getBytes(StandardCharsets.UTF_8));
                            pout3.flush();
                        }
                    } catch (IOException ignored) {
                    }
                }
            });

            // Starting two separate threads to read from the PipedInputStreams
            reader = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!quit) {
                        appendString(textArea, pin);
                    }
                }
            });
            reader.setDaemon(true);
            reader.start();

            reader2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!quit) {
                        appendString(textArea, pin2);
                    }
                }
            });
            reader2.setDaemon(true);
            reader2.start();

            useBlockCaret(true);
        }

        static JavaConsoleFacade getInstance() {
            if (javaConsole == null) {
                javaConsole = new JavaConsole();
            }
            return javaConsole;
        }

        private static void appendString(JTextArea jTextArea, PipedInputStream pipedInputStream) {
            try {
                if (pipedInputStream.available() != 0) {
                    final String input = readLine(pipedInputStream);
                    jTextArea.append(input);
                    jTextArea.setCaretPosition(jTextArea.getDocument().getLength());
                }
            } catch (IOException e) {
                jTextArea.append("\nConsole reports an Internal error.");
                jTextArea.append("The error is: " + e);
                jTextArea.setCaretPosition(jTextArea.getDocument().getLength());
            }
        }

        /**
         * @param in The PipedInputStream to read
         * @return String A line of text
         * @throws IOException
         * @brief Read a line of text from the input stream
         */
        private static String readLine(PipedInputStream in) throws IOException {
            String input = "";
            do {
                int available = in.available();
                if (available == 0) {
                    break;
                }
                byte b[] = new byte[available];
                in.read(b);
                input += new String(b, 0, b.length);
            } while (!input.endsWith("\n") && !input.endsWith("\r\n") && !quit);
            return input;
        }

        /**
         * (non-Javadoc)
         *
         * @see java.awt.event.WindowAdapter#windowClosed(java.awt.event.WindowEvent)
         */
        @Override
        public synchronized void windowClosed(WindowEvent evt) {
            quit = true;
            this.notifyAll(); // stop all threads
            try {
                reader.join(500);
                pin.close();
            } catch (IOException | InterruptedException ignored) {
            }
            try {
                reader2.join(500);
                pin2.close();
            } catch (IOException | InterruptedException ignored) {
            }
            try {
                pout3.close();
            } catch (IOException ignored) {
            }
            System.exit(0);
        }

        /**
         * (non-Javadoc)
         *
         * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
         */
        @Override
        public synchronized void windowClosing(WindowEvent evt) {
            frame.setVisible(false); // default behaviour of JFrame
            frame.dispose();
        }

        /**
         * (non-Javadoc)
         *
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        public synchronized void actionPerformed(ActionEvent evt) {
            this.clear();
        }

        @Override
        public void useBlockCaret(boolean isBlockCaret) {
            if (isBlockCaret) {
                textArea.setCaret(new BlockCaret());
            } else {
                textArea.setCaret(defaultCaret);
            }
        }

        @Override
        public void setCaretVisible(boolean isVisible) {
            textArea.getCaret().setVisible(isVisible);
            textArea.setEditable(true);
        }

        @Override
        public boolean isClearButtonVisible() {
            return button.isVisible();
        }

        @Override
        public void setClearButtonVisible(boolean isVisible) {
            button.setVisible(isVisible);
        }

        /**
         * @brief Clear the console window
         */
        @Override
        public void clear() {
            textArea.setText("");
        }

        /**
         * @return the consol's background color
         */
        @Override
        public Color getBackground() {
            return textArea.getBackground();
        }

        /**
         * @param bg the desired background Color
         */
        @Override
        public void setBackground(Color bg) {
            this.textArea.setBackground(bg);
        }

        /**
         * @return the consol's foreground color
         */
        @Override
        public Color getForeground() {
            return textArea.getForeground();
        }

        /**
         * @param fg the desired foreground Color
         */
        @Override
        public void setForeground(Color fg) {
            this.textArea.setForeground(fg);
            this.textArea.setCaretColor(fg);
        }

        /**
         * @return the consol's font
         */
        @Override
        public Font getFont() {
            return textArea.getFont();
        }

        /**
         * @param f the font to use as the current font
         */
        @Override
        public void setFont(Font f) {
            textArea.setFont(f);
        }

        /**
         * @param i the icon image to display in console window's corner
         */
        @Override
        public void setIconImage(Image i) {
            frame.setIconImage(i);
        }

        /**
         * @param title the console window's title
         */
        @Override
        public void setTitle(String title) {
            frame.setTitle(title);
        }

        @Override
        public void setFontSize(float size) {
            setFont(getFont().deriveFont(size));
        }

        final private class ExJTextArea extends JTextArea {
            @Override
            public void copy() {
                // does nothing
            }

            @Override
            public void cut() {
                // does nothing
            }

            @Override
            public void paste() {
                // does nothing
            }
        }
    }

    /**
     * @author David MacDermot
     * @brief Custom block caret for the Java Console.
     * @par Comments: Adapted from
     * http://www.java2s.com/Code/Java/Swing-JFC/Acustomcaretclass.htm
     * @date 02-07-2012
     * @bug
     */
    class BlockCaret extends DefaultCaret {

        private static final long serialVersionUID = 1L;

        /**
         * @brief Class Constructor
         */
        BlockCaret() {
            // half a second
            setBlinkRate(500);
        }

        /**
         * (non-Javadoc)
         *
         * @see javax.swing.text.DefaultCaret#damage(java.awt.Rectangle)
         */
        @Override
        protected synchronized void damage(Rectangle r) {
            if (r == null) {
                return;
            }
            // give values to x,y,width,height (inherited from java.awt.Rectangle)
            x = r.x;
            y = r.y;
            height = r.height;
            // A value for width was probably set by paint(), which we leave alone.
            // But the first call to damage() precedes the first call to paint(), so
            // in this case we must be prepared to set a valid width, or else
            // paint()
            // will receive a bogus clip area and caret will not get drawn properly.
            if (width <= 0) {
                width = getComponent().getWidth();
            }
            repaint(); //Calls getComponent().repaint(x, y, width, height) to erase
            repaint(); // previous location of caret.  Sometimes one call isn't enough.
        }

        /**
         * (non-Javadoc)
         *
         * @see javax.swing.text.DefaultCaret#paint(java.awt.Graphics)
         */
        @Override
        public void paint(Graphics g) {
            final JTextComponent comp = getComponent();

            if (comp == null) {
                return;
            }

            int dot = getDot();
            Rectangle r;
            char dotChar;
            try {
                r = comp.modelToView(dot);
                if (r == null) {
                    return;
                }
                dotChar = comp.getText(dot, 1).charAt(0);
            } catch (BadLocationException e) {
                return;
            }

            if (Character.isWhitespace(dotChar)) {
                dotChar = '_';
            }

            if ((x != r.x) || (y != r.y)) {
                // paint() has been called directly, without a previous call to
                // damage(), so do some cleanup. (This happens, for example, when
                // the text component is resized.)
                damage(r);
                return;
            }

            g.setColor(comp.getCaretColor());
            g.setXORMode(comp.getBackground()); // do this to draw in XOR mode

            width = g.getFontMetrics().charWidth(dotChar);
            if (isVisible()) {
                g.fillRect(r.x, r.y, width, r.height);
            }
        }
    }
}
