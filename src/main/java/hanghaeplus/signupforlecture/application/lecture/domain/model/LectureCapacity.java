package hanghaeplus.signupforlecture.application.lecture.domain.model;

import lombok.Builder;

import java.util.NoSuchElementException;

@Builder
public record LectureCapacity (

    Long lectureId,
    Lecture lecture,
    int maxSlot,
    int availableSlot
) {
    public LectureCapacity decreaseAvailableSlot() {
        int afterAvailableSlot = this.availableSlot - 1;
        if (afterAvailableSlot < 0) {
            throw new IllegalStateException("Slot을 신청할 수 없습니다.");
        }

        return new LectureCapacity(lectureId, lecture, maxSlot, afterAvailableSlot);
    }
}
