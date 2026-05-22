package io.github.rikkakawaii0612.classtop.ui;

import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class ReminderWindow {
    private final Stage parentStage;
    private final Stage stage;
    private final StackPane root;               // 最外层，控制整体透明度
    private final StackPane contentPane;         // 承载文本和按钮，实现整体缩放和模糊
    private final Scale contentScale;           // 缩放变换
    private final BoxBlur blur;
    private boolean closing = false; // 防止重复关闭

    // 三次缓出插值器：由快至慢
    private static final Interpolator CUBIC_EASE_OUT = new Interpolator() {
        @Override
        protected double curve(double t) {
            return 1 - Math.pow(1 - t, 3);
        }
    };

    // ----- 按钮颜色动画相关 -----
    private static final Color DEFAULT_COLOR = Color.web("#0078d4");
    private static final Color HOVER_COLOR = Color.web("#1e90ff");
    private static final Color PRESS_COLOR = Color.web("#005a9e");
    private final Button closeButton;
    private Timeline colorTimeline;
    private Color currentButtonColor = DEFAULT_COLOR; // 维护当前实际颜色

    private final Timeline focuser;


    public ReminderWindow(Runnable onCloseCallback, Runnable onIgnoreCallback) {
        Stage parentStage = new Stage();
        parentStage.initStyle(StageStyle.UTILITY);
        parentStage.setOpacity(0);
        parentStage.setWidth(1);
        parentStage.setHeight(1);
        parentStage.setX(-10000);
        parentStage.setY(-10000);
        parentStage.show();

        // 创建舞台
        this.stage = new Stage();
        stage.initOwner(parentStage);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);
        stage.setWidth(1600.0D);
        stage.setHeight(900.0D);
        stage.setResizable(false);
        stage.setFullScreenExitHint("");
        stage.setFullScreen(true);

        // ========== 1. 文本和按钮 ==========
        Label title = new Label("请看向讲台上的教师日志");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Microsoft YaHei", 80));
        title.setStyle("-fx-font-weight: bold; -fx-font-smoothing-type: lcd;");

        Label message1 = new Label("不论课程多紧，我们也希望您抽出十秒钟填写教师日志。");
        message1.setTextFill(Color.WHITE);
        message1.setFont(Font.font("Microsoft YaHei", 24));
        message1.setStyle("-fx-font-weight: bold; -fx-font-smoothing-type: lcd;");

        Label message2 = new Label("这将极大便利我们行政组。");
        message2.setTextFill(Color.WHITE);
        message2.setFont(Font.font("Microsoft YaHei", 24));
        message2.setStyle("-fx-font-weight: bold; -fx-font-smoothing-type: lcd;");

        Label ignore = new Label("……没有人来？");
        ignore.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-text-fill: white;" +
                        "-fx-underline: true;" +
                        "-fx-cursor: hand;"
        );
        ignore.setFont(Font.font("Microsoft YaHei", 24));
        ignore.setOnMouseClicked(_ -> startCloseAnimation(onIgnoreCallback));

        // ===== 2. 按钮（颜色由 CSS 驱动，无闪烁） =====
        closeButton = new Button("已填写日志，点击关闭");
        closeButton.setFont(Font.font("Microsoft YaHei", 24));
        // 初始化背景色
        setButtonColor(DEFAULT_COLOR);
        // 其他样式
        closeButton.setStyle(closeButton.getStyle() +
                " -fx-text-fill: white;" +
                " -fx-background-radius: 0;" +
                " -fx-border-radius: 0;" +
                " -fx-border-color: transparent;" +
                " -fx-padding: 15 40 15 40;" +
                " -fx-font-smoothing-type: lcd;"
        );

        // 鼠标事件 → 颜色动画
        closeButton.setOnMouseEntered(_ -> animateButtonColor(HOVER_COLOR, Duration.millis(200)));
        closeButton.setOnMouseExited(_ -> animateButtonColor(DEFAULT_COLOR, Duration.millis(200)));
        closeButton.setOnMousePressed(_ -> animateButtonColor(PRESS_COLOR, Duration.millis(100)));
        closeButton.setOnMouseReleased(_ -> {
            Color target = closeButton.isHover() ? HOVER_COLOR : DEFAULT_COLOR;
            animateButtonColor(target, Duration.millis(100));
        });
        // 点击执行退场动画
        closeButton.setOnAction(_ -> startCloseAnimation(onCloseCallback));

        // ===== 3. 内容面板（StackPane，自带布局，缩放中心为其自身中心） =====
        contentPane = new StackPane();
        contentPane.getChildren().addAll(title, message1, message2, closeButton, ignore);

        StackPane.setAlignment(title, Pos.TOP_CENTER);
        StackPane.setMargin(title, new Insets(180, 0, 0, 0));
        StackPane.setAlignment(message1, Pos.TOP_CENTER);
        StackPane.setMargin(message1, new Insets(330, 0, 0, 0));
        StackPane.setAlignment(message2, Pos.TOP_CENTER);
        StackPane.setMargin(message2, new Insets(390, 0, 0, 0));
        StackPane.setAlignment(closeButton, Pos.BOTTOM_CENTER);
        StackPane.setMargin(closeButton, new Insets(0, 0, 230, 0));
        StackPane.setAlignment(ignore, Pos.BOTTOM_CENTER);
        StackPane.setMargin(ignore, new Insets(0, 0, 190, 0));

        // 模糊效果
        blur = new BoxBlur(0, 0, 1);
        contentPane.setEffect(blur);

        // 缩放变换（pivot 稍后根据 contentPane 大小设置）
        contentScale = new Scale(1, 1);
        contentPane.getTransforms().add(contentScale);

        // 监听 contentPane 的布局变化，动态设置缩放中心（避免震动）
        contentPane.layoutBoundsProperty().addListener((_, _, newBounds) -> {
            contentScale.setPivotX(newBounds.getWidth() / 2);
            contentScale.setPivotY(newBounds.getHeight() / 2);
        });

        // ========== 3. 根布局（透明背景 + 整体透明度）==========
        root = new StackPane(contentPane);
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.65);");
        root.setOpacity(0);  // 初始完全透明

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.show();

        this.parentStage = parentStage;

        stage.setOnCloseRequest(Event::consume);

        // 显示窗口并启动入场动画
        parentStage.show();
        startEntranceAnimation();

        this.focuser = new Timeline(new KeyFrame(Duration.millis(500.0D), _ -> {
            if (!this.stage.isFocused()) {
                this.stage.toFront();
            }
            if (!this.stage.isFullScreen()) {
                this.stage.setFullScreen(true);
            }
        }));
        this.focuser.setCycleCount(Timeline.INDEFINITE); // 设置为无限循环
        this.focuser.play(); // 启动定时器

        this.stage.setOnHiding(_ -> this.focuser.stop());
    }

    // 修改 setButtonColor 方法，同时更新 currentButtonColor
    private void setButtonColor(Color color) {
        currentButtonColor = color;
        String colorString = String.format("#%02X%02X%02X",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
        // 避免每次都拼接整个样式字符串，仅修改背景色
        String baseStyle = "-fx-text-fill: white; -fx-background-radius: 0; -fx-border-radius: 0; -fx-border-color: transparent; -fx-padding: 15 40 15 40;";
        closeButton.setStyle(baseStyle + " -fx-background-color: " + colorString + ";");
    }

    // 平滑动画实现
    private void animateButtonColor(Color target, Duration duration) {
        if (colorTimeline != null && colorTimeline.getStatus() == Animation.Status.RUNNING) {
            colorTimeline.stop();
        }

        final Color start = currentButtonColor;
        final Timeline timeline = new Timeline();
        // 上面的写法不够优雅，直接采用如下标准方式：
        // 创建四个 DoubleProperty 分别代表 R,G,B,opacity 的进度
        DoubleProperty rProgress = new SimpleDoubleProperty();
        DoubleProperty gProgress = new SimpleDoubleProperty();
        DoubleProperty bProgress = new SimpleDoubleProperty();
        DoubleProperty oProgress = new SimpleDoubleProperty();

        // 为每个分量添加监听，实时混合颜色
        ChangeListener<Number> updater = (_, _, _) -> {
            double r = start.getRed() + (target.getRed() - start.getRed()) * rProgress.get();
            double g = start.getGreen() + (target.getGreen() - start.getGreen()) * gProgress.get();
            double b = start.getBlue() + (target.getBlue() - start.getBlue()) * bProgress.get();
            double o = start.getOpacity() + (target.getOpacity() - start.getOpacity()) * oProgress.get();
            setButtonColor(Color.color(r, g, b, o));
        };
        rProgress.addListener(updater);
        gProgress.addListener(updater);
        bProgress.addListener(updater);
        oProgress.addListener(updater);

        KeyFrame kf = new KeyFrame(duration,
                new KeyValue(rProgress, 1.0, CUBIC_EASE_OUT),
                new KeyValue(gProgress, 1.0, CUBIC_EASE_OUT),
                new KeyValue(bProgress, 1.0, CUBIC_EASE_OUT),
                new KeyValue(oProgress, 1.0, CUBIC_EASE_OUT)
        );
        timeline.getKeyFrames().add(kf);
        timeline.setOnFinished(_ -> colorTimeline = null);
        colorTimeline = timeline;
        timeline.play();
    }

    private void startEntranceAnimation() {
        // 获取屏幕中心
        double centerX = stage.getWidth() / 2;
        double centerY = stage.getHeight() / 2;

        // 设定缩放中心为屏幕中心
        contentScale.setPivotX(centerX);
        contentScale.setPivotY(centerY);

        // 布局文本和按钮相对于内容组（相对于屏幕中心偏移）
        // 文本：水平居中，竖直偏上
//        Label title = (Label) contentPane.getChildren().getFirst();
//        title.setLayoutX(centerX - title.getWidth() / 2);
//        title.setLayoutY(250);
//
//        Label message1 = (Label) contentPane.getChildren().get(1);
//        message1.setLayoutX(centerX - message1.getWidth() / 2);
//        message1.setLayoutY(410);
//
//        Label message2 = (Label) contentPane.getChildren().get(2);
//        message2.setLayoutX(centerX - message2.getWidth() / 2);
//        message2.setLayoutY(470);
//
//        Button button = (Button) contentPane.getChildren().get(3);
//        button.setLayoutX(centerX - button.getWidth() / 2);
//        button.setLayoutY(stage.getHeight() - 200);

        // 初始状态：缩放0.5，模糊半径30，根布局透明度0
        contentScale.setX(0.5);
        contentScale.setY(0.5);
        blur.setWidth(30);
        blur.setHeight(30);
        root.setOpacity(0);

        // 构建动画
        Timeline timeline = new Timeline();
        Duration duration = Duration.millis(325);
        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(contentScale.xProperty(), 0.8, CUBIC_EASE_OUT),
                        new KeyValue(contentScale.yProperty(), 0.8, CUBIC_EASE_OUT),
                        new KeyValue(blur.widthProperty(), 30, CUBIC_EASE_OUT),
                        new KeyValue(blur.heightProperty(), 30, CUBIC_EASE_OUT),
                        new KeyValue(root.opacityProperty(), 0, CUBIC_EASE_OUT)
                ),
                new KeyFrame(duration,
                        new KeyValue(contentScale.xProperty(), 1.0, CUBIC_EASE_OUT),
                        new KeyValue(contentScale.yProperty(), 1.0, CUBIC_EASE_OUT),
                        new KeyValue(blur.widthProperty(), 0, CUBIC_EASE_OUT),
                        new KeyValue(blur.heightProperty(), 0, CUBIC_EASE_OUT),
                        new KeyValue(root.opacityProperty(), 1, CUBIC_EASE_OUT)
                )
        );
        timeline.play();
    }

    private void startCloseAnimation(Runnable onCloseCallback) {
        if (closing) return;
        closing = true;

        Timeline timeline = new Timeline();
        Duration duration = Duration.millis(250);  // 关闭稍快
        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(contentScale.xProperty(), 1.0, CUBIC_EASE_OUT),
                        new KeyValue(contentScale.yProperty(), 1.0, CUBIC_EASE_OUT),
                        new KeyValue(blur.widthProperty(), 0, CUBIC_EASE_OUT),
                        new KeyValue(blur.heightProperty(), 0, CUBIC_EASE_OUT),
                        new KeyValue(root.opacityProperty(), 1, CUBIC_EASE_OUT)
                ),
                new KeyFrame(duration,
                        new KeyValue(contentScale.xProperty(), 0.8, CUBIC_EASE_OUT),
                        new KeyValue(contentScale.yProperty(), 0.8, CUBIC_EASE_OUT),
                        new KeyValue(blur.widthProperty(), 30, CUBIC_EASE_OUT),
                        new KeyValue(blur.heightProperty(), 30, CUBIC_EASE_OUT),
                        new KeyValue(root.opacityProperty(), 0, CUBIC_EASE_OUT)
                )
        );
        timeline.setOnFinished(_ -> {
            stage.close();
            if (onCloseCallback != null) {
                onCloseCallback.run();
            }
        });
        timeline.play();
    }

    public boolean isShowing() {
        return parentStage.isShowing();
    }

    public void close() {
        // 外部调用直接关闭（跳过动画）
        closing = true;
        parentStage.close();
    }
}