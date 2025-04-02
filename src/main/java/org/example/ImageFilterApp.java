package org.example;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class ImageFilterApp extends JFrame {
    private BufferedImage originalImage;
    private BufferedImage filteredImage;
    private JLabel imageLabel;
    private JComboBox<String> filterTypeComboBox;
    private JComboBox<Integer> kernelSizeComboBox;
    private JTextField sigmaField;
    private JButton loadButton;
    private JButton applyFilterButton;
    private JButton resetButton;
    private JButton saveButton;
    private JPanel sigmaPanel;

    private JSplitPane splitPane;
    private JPanel controlPanel;
    private JButton collapseButton;
    private boolean controlPanelCollapsed = false;
    private int lastDividerLocation = 250;

    public ImageFilterApp() {
        setTitle("Програма фільтрації зображень");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);

        // Створюємо панель керування
        createControlPanel();

        // Створюємо область відображення зображення
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        JScrollPane scrollPane = new JScrollPane(imageLabel);

        // Створюємо розділювач (SplitPane)
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, controlPanel, scrollPane);
        splitPane.setDividerLocation(lastDividerLocation);
        splitPane.setOneTouchExpandable(true);

        add(splitPane, BorderLayout.CENTER);

        // Додаємо слухачів подій
        setupEventListeners();
    }


    // 1) Переробити інтерфейс вертикально
    // Створення панелі керування
    private void createControlPanel() {
        controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Панель із налаштуваннями
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Створюємо кнопки та елементи керування
        loadButton = new JButton("Завантажити зображення");
        filterTypeComboBox = new JComboBox<>(new String[]{"Усереднюючий фільтр", "Медіанний фільтр", "Фільтр Гауса"});
        kernelSizeComboBox = new JComboBox<>(new Integer[]{3, 5, 7, 9});
        applyFilterButton = new JButton("Застосувати фільтр");
        resetButton = new JButton("Скинути до оригіналу");
        saveButton = new JButton("Зберегти зображення");
        collapseButton = new JButton("<<");

        // Розташовуємо елементи в панелі
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        settingsPanel.add(loadButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        settingsPanel.add(new JLabel("Тип фільтра:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        settingsPanel.add(filterTypeComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        settingsPanel.add(new JLabel("Розмір ядра:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        settingsPanel.add(kernelSizeComboBox, gbc);

        // Панель для параметра sigma
        sigmaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sigmaPanel.add(new JLabel("Sigma:"));
        sigmaField = new JTextField("1.0", 5);
        sigmaPanel.add(sigmaField);
        sigmaPanel.setVisible(false); // Початково приховано

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        settingsPanel.add(sigmaPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        settingsPanel.add(applyFilterButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        settingsPanel.add(resetButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        settingsPanel.add(saveButton, gbc);

        // Створюємо окрему панель для заголовка і кнопки згортання
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(collapseButton, BorderLayout.EAST);
        headerPanel.add(new JLabel("Панель керування"), BorderLayout.CENTER);

        // Розміщуємо компоненти в панелі керування
        controlPanel.add(headerPanel, BorderLayout.NORTH);
        controlPanel.add(settingsPanel, BorderLayout.CENTER);

        // Початково відключаємо кнопки фільтра, скидання та збереження
        applyFilterButton.setEnabled(false);
        resetButton.setEnabled(false);
        saveButton.setEnabled(false);
    }

    private void setupEventListeners() {
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadImage();
            }
        });

        filterTypeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Показуємо поле sigma тільки для фільтра Гауса
                String selectedFilter = (String) filterTypeComboBox.getSelectedItem();
                sigmaPanel.setVisible("Фільтр Гауса".equals(selectedFilter));
                controlPanel.revalidate();
                controlPanel.repaint();
            }
        });

        applyFilterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyFilter();
            }
        });

        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetToOriginal();
            }
        });

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveImage();
            }
        });

        collapseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleControlPanel();
            }
        });
    }

    private void toggleControlPanel() {
        if (!controlPanelCollapsed) {
            // Зберігаємо поточну позицію розділювача перед згортанням
            lastDividerLocation = splitPane.getDividerLocation();
            splitPane.setDividerLocation(collapseButton.getWidth() + 20);
            collapseButton.setText(">>");
            controlPanelCollapsed = true;

            // Приховуємо всі елементи керування, крім кнопки згортання
            setControlsVisible(false);
        } else {
            // Відновлюємо збережену позицію розділювача
            splitPane.setDividerLocation(lastDividerLocation);
            collapseButton.setText("<<");
            controlPanelCollapsed = false;

            // Показуємо елементи керування
            setControlsVisible(true);
        }
    }

    private void setControlsVisible(boolean visible) {
        // Отримуємо всі компоненти з controlPanel, крім кнопки згортання
        Component[] components = controlPanel.getComponents();
        for (Component component : components) {
            if (component instanceof JPanel && component != collapseButton.getParent()) {
                // Встановлюємо видимість для всіх панелей, крім тієї, де кнопка згортання
                component.setVisible(visible);
            }
        }

        // Перекомпонуємо панель керування
        controlPanel.revalidate();
        controlPanel.repaint();
    }

    private void loadImage() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Зображення", "jpg", "jpeg", "png", "bmp", "gif");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                originalImage = ImageIO.read(selectedFile);
                displayImage(originalImage);
                applyFilterButton.setEnabled(true);
                resetButton.setEnabled(true);

                saveButton.setEnabled(false);
                filteredImage = null;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Помилка завантаження зображення: " + e.getMessage(),
                        "Помилка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void applyFilter() {
        if (originalImage == null) {
            return;
        }

        String filterType = (String) filterTypeComboBox.getSelectedItem();
        int kernelSize = (Integer) kernelSizeComboBox.getSelectedItem();

        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); // Змінюємо курсор на "очікування"

            if ("Усереднюючий фільтр".equals(filterType)) {
                filteredImage = applyBoxFilter(originalImage, kernelSize);
            } else if ("Медіанний фільтр".equals(filterType)) {
                filteredImage = applyMedianFilter(originalImage, kernelSize);
            } else if ("Фільтр Гауса".equals(filterType)) {
                double sigma = Double.parseDouble(sigmaField.getText());
                filteredImage = applyGaussianFilter(originalImage, kernelSize, sigma);
            }

            displayImage(filteredImage);
            saveButton.setEnabled(true);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Неправильне значення sigma. Введіть числове значення.",
                    "Помилка", JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void saveImage() {
        if (filteredImage == null) {
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "PNG зображення", "png");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            // Додаємо розширення .png, якщо воно відсутнє
            if (!file.getName().toLowerCase().endsWith(".png")) {
                file = new File(file.getAbsolutePath() + ".png");
            }

            try {
                ImageIO.write(filteredImage, "png", file);
                JOptionPane.showMessageDialog(this,
                        "Зображення успішно збережено!",
                        "Інформація", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Помилка збереження зображення: " + e.getMessage(),
                        "Помилка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void displayImage(BufferedImage image) {
        ImageIcon icon = new ImageIcon(image);
        imageLabel.setIcon(icon);
        imageLabel.revalidate();
        imageLabel.repaint();
    }

    /*
     * Усереднюючий фільтр (Box filter):
     * Для кожного пікселя вибирається квадратна область (ядро).
     * Обчислюється середнє значення кольорів усіх пікселів у цій області.
     * Отримане середнє значення встановлюється як новий колір центрального пікселя.
     */
    private BufferedImage applyBoxFilter(BufferedImage input, int kernelSize) {
        int width = input.getWidth();
        int height = input.getHeight();

        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int radius = kernelSize / 2;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int redSum = 0, greenSum = 0, blueSum = 0;
                int pixelCount = 0;

                // Обробка околиці
                for (int ky = -radius; ky <= radius; ky++) {
                    for (int kx = -radius; kx <= radius; kx++) {
                        int pixelX = Math.min(Math.max(x + kx, 0), width - 1);
                        int pixelY = Math.min(Math.max(y + ky, 0), height - 1);

                        Color pixelColor = new Color(input.getRGB(pixelX, pixelY));
                        redSum += pixelColor.getRed();
                        greenSum += pixelColor.getGreen();
                        blueSum += pixelColor.getBlue();
                        pixelCount++;
                    }
                }

                // Середні значення
                int red = redSum / pixelCount;
                int green = greenSum / pixelCount;
                int blue = blueSum / pixelCount;

                Color newColor = new Color(red, green, blue);
                output.setRGB(x, y, newColor.getRGB());
            }
        }

        return output;
    }

    /*
     * Медіанний фільтр (Median filter):
     * Для кожного пікселя вибирається квадратна область (ядро).
     * Всі кольори (червоний, зелений, синій) пікселів у цій області записуються в масиви.
     * Масиви сортуються, і вибирається середній (медіанний) елемент.
     * Отримане значення встановлюється як новий колір центрального пікселя.
     */

    private BufferedImage applyMedianFilter(BufferedImage input, int kernelSize) {
        int width = input.getWidth();
        int height = input.getHeight();

        BufferedImage output = new BufferedImage(width, height, input.getType());

        int radius = kernelSize / 2;
        int arraySize = kernelSize * kernelSize;

        int[] redValues = new int[arraySize];
        int[] greenValues = new int[arraySize];
        int[] blueValues = new int[arraySize];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = 0;

                // Збираємо значення пікселів у ядрі
                for (int ky = -radius; ky <= radius; ky++) {
                    for (int kx = -radius; kx <= radius; kx++) {
                        int pixelX = Math.min(Math.max(x + kx, 0), width - 1);
                        int pixelY = Math.min(Math.max(y + ky, 0), height - 1);

                        Color pixelColor = new Color(input.getRGB(pixelX, pixelY));
                        redValues[index] = pixelColor.getRed();
                        greenValues[index] = pixelColor.getGreen();
                        blueValues[index] = pixelColor.getBlue();
                        index++;
                    }
                }

                // Сортуємо масиви та отримуємо медіанні значення
                Arrays.sort(redValues);
                Arrays.sort(greenValues);
                Arrays.sort(blueValues);

                int medianIndex = arraySize / 2;
                int red = redValues[medianIndex];
                int green = greenValues[medianIndex];
                int blue = blueValues[medianIndex];

                Color newColor = new Color(red, green, blue);
                output.setRGB(x, y, newColor.getRGB());
            }
        }

        return output;
    }

    // Фільтр Гауса
    /*
     * Фільтр Гауса:
     * Для кожного пікселя зображення обчислюється нове значення на основі сусідніх пікселів,
     * враховуючи ваги, що залежать від відстані до центрального пікселя в ядрі Гауса.
     */
    private BufferedImage applyGaussianFilter(BufferedImage input, int kernelSize, double sigma) {
        int width = input.getWidth();
        int height = input.getHeight();

        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Створюємо ядро фільтра Гауса
        double[][] kernel = createGaussianKernel(kernelSize, sigma);

        int radius = kernelSize / 2;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double redSum = 0, greenSum = 0, blueSum = 0;

                // Застосовуємо ядро до околиці
                for (int ky = -radius; ky <= radius; ky++) {
                    for (int kx = -radius; kx <= radius; kx++) {
                        int pixelX = Math.min(Math.max(x + kx, 0), width - 1);
                        int pixelY = Math.min(Math.max(y + ky, 0), height - 1);

                        // Отримуємо вагу з попередньо розрахованого ядра
                        double weight = kernel[ky + radius][kx + radius];

                        Color pixelColor = new Color(input.getRGB(pixelX, pixelY));
                        redSum += pixelColor.getRed() * weight;
                        greenSum += pixelColor.getGreen() * weight;
                        blueSum += pixelColor.getBlue() * weight;
                    }
                }

                // Округлюємо значення до цілих
                int red = (int) Math.round(redSum);
                int green = (int) Math.round(greenSum);
                int blue = (int) Math.round(blueSum);

                // Обмежуємо значення RGB до діапазону 0-255
                red = Math.min(255, Math.max(0, red));
                green = Math.min(255, Math.max(0, green));
                blue = Math.min(255, Math.max(0, blue));

                Color newColor = new Color(red, green, blue);
                output.setRGB(x, y, newColor.getRGB());
            }
        }

        return output;
    }

    // Створення ядра фільтра Гауса
    private double[][] createGaussianKernel(int size, double sigma) {
        double[][] kernel = new double[size][size];
        int radius = size / 2;
        double sum = 0.0;

        // Обчислюємо значення ядра за формулою Гауса
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                // Формула Гауса: e^(-(x^2+y^2)/(2*sigma^2))
                double exponent = -(x * x + y * y) / (2 * sigma * sigma);
                kernel[y + radius][x + radius] = Math.exp(exponent);
                sum += kernel[y + radius][x + radius];
            }
        }

        // Нормалізуємо ядро, щоб сума всіх елементів дорівнювала 1
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                kernel[y][x] /= sum;
            }
        }

        return kernel;
    }

    private void resetToOriginal() {
        if (originalImage == null) {
            return;
        }

        // Повертаємо оригінальне зображення
        displayImage(originalImage);
        // Очищаємо відфільтроване зображення
        filteredImage = null;
        // Відключаємо кнопку збереження, оскільки відображається оригінал
        saveButton.setEnabled(false);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ImageFilterApp().setVisible(true);
            }
        });
    }
}