package hanghaeplus.signupforlecture.application.lecture.facade;

import hanghaeplus.signupforlecture.application.lecture.domain.model.Lecture;
import hanghaeplus.signupforlecture.application.lecture.domain.model.LectureCapacity;
import hanghaeplus.signupforlecture.application.lecture.dto.request.LectureApplyRequestDto;
import hanghaeplus.signupforlecture.application.lecture.dto.request.LectureAvailableRequestDto;
import hanghaeplus.signupforlecture.application.lecture.dto.response.LectureAvailableResponseDto;
import hanghaeplus.signupforlecture.application.lecture.dto.response.LectureSignedUpResponseDto;
import hanghaeplus.signupforlecture.application.lecture.service.LectureApplyHistoryService;
import hanghaeplus.signupforlecture.application.lecture.service.LectureCapacityService;
import hanghaeplus.signupforlecture.application.lecture.service.LectureService;
import hanghaeplus.signupforlecture.application.user.domain.model.User;
import hanghaeplus.signupforlecture.application.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LectureFacade {

    private final UserService userService;

    private final LectureService lectureService;
    private final LectureApplyHistoryService lectureApplyHistoryService;
    private final LectureCapacityService lectureCapacityService;

    public List<LectureAvailableResponseDto> getAvailableLectures(LectureAvailableRequestDto lectureAvailableRequestDto) {
        List<Lecture> availableLectures = lectureService.getAvailableLectures(lectureAvailableRequestDto.requestDate());

        return availableLectures.stream()
                .map(LectureAvailableResponseDto::fromDomain)
                .toList();
    }

    public List<LectureSignedUpResponseDto> getSignedUpLectures(Long userId) {
        User user = userService.getUser(userId);

        List<Long> signedUpLectureIds = lectureApplyHistoryService.findSignedUpLectureHistories(user.id());
        List<Lecture> signedUpLectures = lectureService.findSignedUpLectures(signedUpLectureIds);

        return signedUpLectures.stream()
                .map(LectureSignedUpResponseDto::fromDomain)
                .toList();
    }

    @Transactional
    public void applyLecture(Long lectureId, LectureApplyRequestDto lectureApplyRequestDto) {
        User user = userService.getUser(lectureApplyRequestDto.userId());
        Lecture lecture = lectureService.getLecture(lectureId);

        try {
            lectureApplyHistoryService.checkApplyLectureHistory(lecture.id(), user.id());

            // 락
            // 신청 가능한 Slot
            LectureCapacity lectureCapacity = lectureCapacityService.getAvailableSlotLock(lecture.id());
            lectureApplyHistoryService.insertAppliedHistory(lecture, lectureApplyRequestDto.userId());

            // 신청 가능 Slot - 1 >> 저장
            lectureCapacityService.applyAvailableSlot(lectureCapacity);
        } catch (RuntimeException e) {
            lectureApplyHistoryService.insertFailedHistory(lecture, lectureApplyRequestDto.userId());
        }
    }
}
