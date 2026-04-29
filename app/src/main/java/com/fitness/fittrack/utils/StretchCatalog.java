package com.fitness.fittrack.utils;

public final class StretchCatalog {
    private static final StretchMove[] PUSHUP_MOVES = {
            new StretchMove("Xoay cổ tay và vai", "Xoay cổ tay, mở rộng vai, hít thở chậm trước hiệp tiếp theo.", 30),
            new StretchMove("Giãn ngực", "Đưa hai tay ra sau, mở ngực nhẹ và giữ lưng thẳng.", 30),
            new StretchMove("Thả lỏng cổ tay", "Ép nhẹ lòng bàn tay và mu bàn tay để giảm căng cổ tay.", 30)
    };

    private static final StretchMove[] SITUP_MOVES = {
            new StretchMove("Giãn cơ bụng", "Nằm sấp, chống tay nâng ngực nhẹ để giãn cơ bụng.", 30),
            new StretchMove("Xoay lưng nhẹ", "Ngồi thẳng, xoay thân sang trái rồi sang phải thật chậm.", 30),
            new StretchMove("Giãn hông", "Ngồi gập một chân, đẩy hông nhẹ để thả lỏng vùng hông.", 30)
    };

    private static final StretchMove[] RUNNING_MOVES = {
            new StretchMove("Giãn bắp chân", "Đưa một chân ra sau, ấn gót chân xuống sàn và đổi bên.", 30),
            new StretchMove("Giãn đùi trước", "Nắm cổ chân, kéo gót về mông để giãn mặt trước đùi.", 30),
            new StretchMove("Xoay cổ chân", "Xoay cổ chân mỗi bên vài vòng trước khi tiếp tục.", 30)
    };

    private StretchCatalog() {}

    public static StretchMove forSet(String type, int completedSet) {
        StretchMove[] moves;
        if ("pushup".equals(type)) {
            moves = PUSHUP_MOVES;
        } else if ("situp".equals(type)) {
            moves = SITUP_MOVES;
        } else {
            moves = RUNNING_MOVES;
        }

        int index = Math.max(0, completedSet - 1) % moves.length;
        return moves[index];
    }

    public static class StretchMove {
        private final String title;
        private final String instruction;
        private final int durationSeconds;

        StretchMove(String title, String instruction, int durationSeconds) {
            this.title = title;
            this.instruction = instruction;
            this.durationSeconds = durationSeconds;
        }

        public String getTitle() {
            return title;
        }

        public String getInstruction() {
            return instruction;
        }

        public int getDurationSeconds() {
            return durationSeconds;
        }
    }
}
