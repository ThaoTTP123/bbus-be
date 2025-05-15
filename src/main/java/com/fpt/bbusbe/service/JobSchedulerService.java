package com.fpt.bbusbe.service;

import com.fpt.bbusbe.firebase.FirebaseMessagingService;
import com.fpt.bbusbe.model.entity.Event;
import com.fpt.bbusbe.model.entity.Parent;
import com.fpt.bbusbe.model.entity.User;
import com.fpt.bbusbe.repository.ParentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
@RequiredArgsConstructor
public class JobSchedulerService {

    private final TaskScheduler taskScheduler;
    private final FirebaseMessagingService firebaseMessagingService;

    private final ConcurrentHashMap<String, ScheduledFuture<?>> scheduledJobs = new ConcurrentHashMap<>();
    private final ParentRepository parentRepository;

    public void schedulePickupDeadlineEvent(Event event) {
//        event.setStart(new Date(event.getStart().getTime() - (7 * 60 * 60 * 1000)));
//        event.setEnd(new Date(event.getEnd().getTime() - (7 * 60 * 60 * 1000)));
        if (event.getName() != null && event.getName().startsWith("Set up time registration")) {

            cancelScheduledJobs(event.getId()); // Cancel any previously scheduled jobs first

            // Schedule START job
            if (event.getStart() != null && event.getStart().after(new Date())) {
                Runnable startTask = () -> {
                    firebaseMessagingService.sendNotificationToMultipleUsers(
                            getAllParentTokens(),
                            "Bắt đầu đăng ký điểm đón",
                            "Bạn có thể bắt đầu đăng ký điểm đón cho năm học mới."
                    );
                    System.out.println("📢 Start job executed for: " + event.getName());
                };

                ScheduledFuture<?> future = taskScheduler.schedule(startTask, event.getStart());
                scheduledJobs.put(event.getId() + "-start", future);
                System.out.println("📢 Start job scheduled for: " + event.getName() + " at " + event.getStart());
            }

            // Schedule END job
            if (event.getEnd() != null && event.getEnd().after(new Date())) {
                Runnable endTask = () -> {
                    firebaseMessagingService.sendNotificationToMultipleUsers(
                            getAllParentTokens(),
                            "Kết thúc đăng ký điểm đón",
                            "Thời hạn đăng ký điểm đón đã kết thúc. Vui lòng kiểm tra lại thông tin."
                    );
                    System.out.println("📢 End job executed for: " + event.getName());
                };

                ScheduledFuture<?> future = taskScheduler.schedule(endTask, event.getEnd());
                scheduledJobs.put(event.getId() + "-end", future);
                System.out.println("📢 End job scheduled for: " + event.getName() + " at " + event.getEnd());
            }
        }
    }

    private void cancelScheduledJobs(UUID eventId) {
        ScheduledFuture<?> startFuture = scheduledJobs.remove(eventId + "-start");
        if (startFuture != null) {
            startFuture.cancel(false);
        }

        ScheduledFuture<?> endFuture = scheduledJobs.remove(eventId + "-end");
        if (endFuture != null) {
            endFuture.cancel(false);
        }
    }

    private List<String> getAllParentTokens() {
        // Implement this method to retrieve all parent tokens from your database
        List<Parent> parents = parentRepository.findAllCustom();
        if (!parents.isEmpty()) {
            return parents.stream()
                    .map(Parent::getUser)
                    .map(User::getDeviceToken)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
        }
        return List.of();
    }
}

