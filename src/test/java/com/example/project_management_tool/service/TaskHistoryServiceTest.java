package com.example.project_management_tool.service;

import com.example.project_management_tool.entity.TaskHistory;
import com.example.project_management_tool.model.TaskModel;
import com.example.project_management_tool.repository.TaskHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * ✅ TaskHistoryServiceTest orienté BRANCHES (JaCoCo)
 * Couvre :
 * - old == new (y compris null/null) -> ne sauvegarde pas
 * - conversion String.valueOf (ex: 1 vs "1") -> ne sauvegarde pas
 * - old != new -> sauvegarde
 * - modifiedBy null / blank -> "SYSTEM"
 * - modifiedBy normal -> valeur conservée
 */
@ExtendWith(MockitoExtension.class)
class TaskHistoryServiceTest {

    @Mock
    private TaskHistoryRepository taskHistoryRepository;

    @InjectMocks
    private TaskHistoryService taskHistoryService;

    private TaskModel task;

    @BeforeEach
    void setUp() {
        task = new TaskModel();
        task.setId(6L);
        task.setTitle("Ancien titre");
    }

    @Test
    void recordChange_shouldNotSave_whenBothValuesNull() {
        taskHistoryService.recordChange(task, "dueDate", null, null, "API");

        verify(taskHistoryRepository, never()).save(any(TaskHistory.class));
    }

    @Test
    void recordChange_shouldNotSave_whenValuesEqual_sameString() {
        taskHistoryService.recordChange(task, "title", "Same", "Same", "API");

        verify(taskHistoryRepository, never()).save(any(TaskHistory.class));
    }

    @Test
    void recordChange_shouldNotSave_whenValuesEqual_afterStringConversion() {
        // oldValue=1 -> "1", newValue="1" -> "1" => Objects.equals => true
        taskHistoryService.recordChange(task, "priority", 1, "1", "API");

        verify(taskHistoryRepository, never()).save(any(TaskHistory.class));
    }

    @Test
    void recordChange_shouldSave_whenDifferent_andKeepModifiedBy() {
        ArgumentCaptor<TaskHistory> captor = ArgumentCaptor.forClass(TaskHistory.class);

        taskHistoryService.recordChange(task, "title", "Ancien titre", "Nouveau titre", "API");

        verify(taskHistoryRepository).save(captor.capture());
        TaskHistory saved = captor.getValue();

        assertThat(saved.getTask()).isSameAs(task);
        assertThat(saved.getFieldName()).isEqualTo("title");
        assertThat(saved.getOldValue()).isEqualTo("Ancien titre");
        assertThat(saved.getNewValue()).isEqualTo("Nouveau titre");
        assertThat(saved.getModifiedBy()).isEqualTo("API");
        assertThat(saved.getModifiedAt()).isNotNull();
        assertThat(saved.getModifiedAt()).isBefore(LocalDateTime.now().plusSeconds(2));
    }

    @Test
    void recordChange_shouldSave_andDefaultModifiedByToSYSTEM_whenNull() {
        ArgumentCaptor<TaskHistory> captor = ArgumentCaptor.forClass(TaskHistory.class);

        taskHistoryService.recordChange(task, "status", "EN_ATTENTE", "EN_COURS", null);

        verify(taskHistoryRepository).save(captor.capture());
        TaskHistory saved = captor.getValue();

        assertThat(saved.getModifiedBy()).isEqualTo("SYSTEM");
        assertThat(saved.getOldValue()).isEqualTo("EN_ATTENTE");
        assertThat(saved.getNewValue()).isEqualTo("EN_COURS");
    }

    @Test
    void recordChange_shouldSave_andDefaultModifiedByToSYSTEM_whenBlank() {
        ArgumentCaptor<TaskHistory> captor = ArgumentCaptor.forClass(TaskHistory.class);

        taskHistoryService.recordChange(task, "description", "A", "B", "   ");

        verify(taskHistoryRepository).save(captor.capture());
        TaskHistory saved = captor.getValue();

        assertThat(saved.getModifiedBy()).isEqualTo("SYSTEM");
        assertThat(saved.getOldValue()).isEqualTo("A");
        assertThat(saved.getNewValue()).isEqualTo("B");
    }
}
