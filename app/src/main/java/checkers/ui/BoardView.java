package checkers.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Outline;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.AccelerateDecelerateInterpolator;
import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import checkers.R;
import checkers.game.Board;
import checkers.game.Piece;
import checkers.game.Pos;
import checkers.game.Side;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class BoardView extends View {

    public interface OnSquareTapListener {
        void onSquareTapped(Pos pos);
    }

    private OnSquareTapListener listener;

    private Board board = Board.initial();
    private List<Pos> selectedPath = Collections.emptyList();
    private Set<Pos> targets = Collections.emptySet();
    private Set<Pos> hintPieces = Collections.emptySet();
    private List<Pos> lastMovePath = Collections.emptyList();

    private boolean firstRender = true;
    private ValueAnimator animator;
    private float animProgress = 1f;
    private List<Pos> animPath = Collections.emptyList();
    private Piece animPiece;

    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint body = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labels = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path crown = new Path();
    private Shader whiteShader;
    private Shader blackShader;

    public BoardView(Context context) {
        this(context, null);
    }

    public BoardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        fill.setStyle(Paint.Style.FILL);
        stroke.setStyle(Paint.Style.STROKE);
        body.setStyle(Paint.Style.FILL);
        labels.setTypeface(Typeface.DEFAULT_BOLD);

        final float radius = dp(14);
        GradientDrawable frame = new GradientDrawable();
        frame.setColor(color(R.color.board_frame));
        frame.setCornerRadius(radius);
        setBackground(frame);
        setElevation(dp(6));
        setClipToOutline(true);
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
            }
        });
    }

    public void setOnSquareTapListener(OnSquareTapListener listener) {
        this.listener = listener;
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private int color(@ColorRes int id) {
        return ContextCompat.getColor(getContext(), id);
    }

    public void render(Board board, List<Pos> selectedPath, Set<Pos> targets,
                       Set<Pos> hintPieces, List<Pos> lastMovePath) {
        boolean moveChanged = !firstRender && lastMovePath.size() >= 2 && !lastMovePath.equals(this.lastMovePath);
        firstRender = false;

        if (animator != null && (moveChanged || lastMovePath.isEmpty())) animator.cancel();

        this.board = board;
        this.selectedPath = selectedPath;
        this.targets = targets;
        this.hintPieces = hintPieces;
        this.lastMovePath = lastMovePath;

        if (moveChanged) {
            Piece moved = board.get(lastMovePath.get(lastMovePath.size() - 1));
            if (moved != null) startAnimation(lastMovePath, moved);
        }
        invalidate();
    }

    private void startAnimation(List<Pos> path, Piece piece) {
        animPath = path;
        animPiece = piece;
        animProgress = 0f;
        ValueAnimator a = ValueAnimator.ofFloat(0f, 1f);
        a.setDuration(180L + 120L * (path.size() - 2));
        a.setInterpolator(new AccelerateDecelerateInterpolator());
        a.addUpdateListener(animation -> {
            animProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        a.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animPiece = null;
                invalidate();
            }
        });
        animator = a;
        a.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animator != null) animator.cancel();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float r = (float) w / Board.SIZE * 0.38f;
        whiteShader = new RadialGradient(-r * 0.35f, -r * 0.4f, r * 1.5f,
                color(R.color.piece_white_light), color(R.color.piece_white_dark), Shader.TileMode.CLAMP);
        blackShader = new RadialGradient(-r * 0.35f, -r * 0.4f, r * 1.5f,
                color(R.color.piece_black_light), color(R.color.piece_black_dark), Shader.TileMode.CLAMP);
        labels.setTextSize((float) w / Board.SIZE * 0.17f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float cell = (float) getWidth() / Board.SIZE;
        float pad = cell * 0.06f;

        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Pos pos = new Pos(row, col);
                float left = col * cell;
                float top = row * cell;

                fill.setColor(color(pos.isDark() ? R.color.board_dark : R.color.board_light));
                canvas.drawRect(left, top, left + cell, top + cell, fill);

                if (lastMovePath.contains(pos)) drawOverlay(canvas, left, top, cell, R.color.highlight_last_move);
                if (selectedPath.contains(pos)) drawOverlay(canvas, left, top, cell, R.color.highlight_selected);

                labels.setColor(color(pos.isDark() ? R.color.board_light : R.color.board_dark));
                if (row == Board.SIZE - 1) {
                    labels.setTextAlign(Paint.Align.RIGHT);
                    canvas.drawText(String.valueOf((char) ('a' + col)), left + cell - pad, top + cell - pad, labels);
                }
                if (col == 0) {
                    labels.setTextAlign(Paint.Align.LEFT);
                    canvas.drawText(String.valueOf(Board.SIZE - row), left + pad, top + labels.getTextSize() + pad, labels);
                }

                Piece piece = board.get(pos);
                boolean beingAnimated = animPiece != null && pos.equals(animPath.get(animPath.size() - 1));
                if (piece != null && !beingAnimated) {
                    drawPiece(canvas, left + cell / 2, top + cell / 2, cell, piece);
                }
                if (hintPieces.contains(pos)) {
                    stroke.setColor(color(R.color.highlight_hint));
                    stroke.setStrokeWidth(cell * 0.06f);
                    canvas.drawCircle(left + cell / 2, top + cell / 2, cell * 0.44f, stroke);
                }
                if (targets.contains(pos)) {
                    fill.setColor(color(R.color.highlight_target));
                    canvas.drawCircle(left + cell / 2, top + cell / 2, cell * 0.16f, fill);
                }
            }
        }

        if (animPiece != null) {
            int segments = animPath.size() - 1;
            float t = animProgress * segments;
            int i = Math.min((int) t, segments - 1);
            float f = t - i;
            Pos from = animPath.get(i);
            Pos to = animPath.get(i + 1);
            float cx = (from.col + (to.col - from.col) * f + 0.5f) * cell;
            float cy = (from.row + (to.row - from.row) * f + 0.5f) * cell;
            drawPiece(canvas, cx, cy, cell, animPiece);
        }
    }

    private void drawOverlay(Canvas canvas, float left, float top, float cell, @ColorRes int colorRes) {
        fill.setColor(color(colorRes));
        canvas.drawRect(left, top, left + cell, top + cell, fill);
    }

    private void drawPiece(Canvas canvas, float cx, float cy, float cell, Piece piece) {
        boolean white = piece.getSide() == Side.WHITE;
        float r = cell * 0.38f;

        canvas.save();
        canvas.translate(cx, cy);

        fill.setColor(color(R.color.piece_shadow));
        canvas.drawCircle(0, r * 0.14f, r, fill);

        body.setShader(white ? whiteShader : blackShader);
        canvas.drawCircle(0, 0, r, body);
        body.setShader(null);

        stroke.setColor(color(white ? R.color.piece_white_edge : R.color.piece_black_edge));
        stroke.setStrokeWidth(cell * 0.03f);
        canvas.drawCircle(0, 0, r, stroke);
        stroke.setColor(color(white ? R.color.piece_white_edge : R.color.piece_black_ring));
        stroke.setStrokeWidth(cell * 0.025f);
        canvas.drawCircle(0, 0, r * 0.7f, stroke);

        if (piece.isKing()) {
            float w = cell * 0.17f;
            float h = cell * 0.12f;
            crown.reset();
            crown.moveTo(-w, h);
            crown.lineTo(-w, -h * 0.7f);
            crown.lineTo(-w / 2, 0);
            crown.lineTo(0, -h);
            crown.lineTo(w / 2, 0);
            crown.lineTo(w, -h * 0.7f);
            crown.lineTo(w, h);
            crown.close();
            fill.setColor(color(R.color.king_mark));
            canvas.drawPath(crown, fill);
            stroke.setColor(color(R.color.king_edge));
            stroke.setStrokeWidth(cell * 0.02f);
            canvas.drawPath(crown, stroke);
        }
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) return true;
        if (event.getAction() == MotionEvent.ACTION_UP) {
            float cell = (float) getWidth() / Board.SIZE;
            Pos pos = new Pos((int) (event.getY() / cell), (int) (event.getX() / cell));
            if (pos.inBounds() && listener != null) listener.onSquareTapped(pos);
            performClick();
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }
}
