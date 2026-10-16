package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScholarDao {

    // User Account
    @Query("SELECT * FROM user_accounts WHERE id = 1 LIMIT 1")
    fun observeActiveAccount(): Flow<UserAccountEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserAccount(account: UserAccountEntity)

    @Query("DELETE FROM user_accounts")
    suspend fun clearUserAccount()

    // One-Time 2-Minute Student Access Lock
    @Query("SELECT * FROM student_access_locks WHERE studentCode = :studentCode LIMIT 1")
    suspend fun getStudentAccessLock(studentCode: String): StudentAccessLockEntity?

    @Query("SELECT * FROM student_access_locks")
    fun observeAllStudentLocks(): Flow<List<StudentAccessLockEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveStudentAccessLock(lock: StudentAccessLockEntity)

    // Class Diary
    @Query("SELECT * FROM diary_entries ORDER BY timestamp DESC")
    fun observeDiaryEntries(): Flow<List<DiaryEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiaryEntry(entry: DiaryEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiaryEntries(entries: List<DiaryEntryEntity>)

    @Update
    suspend fun updateDiaryEntry(entry: DiaryEntryEntity)

    @Query("SELECT COUNT(*) FROM diary_entries")
    suspend fun getDiaryCount(): Int

    @Query("DELETE FROM diary_entries WHERE id = :id")
    suspend fun deleteDiaryEntry(id: Int)

    // Announcements
    @Query("SELECT * FROM announcements ORDER BY isPinned DESC, timestamp DESC")
    fun observeAnnouncements(): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncements(announcements: List<AnnouncementEntity>)

    @Update
    suspend fun updateAnnouncement(announcement: AnnouncementEntity)

    @Query("DELETE FROM announcements WHERE id = :id")
    suspend fun deleteAnnouncement(id: Int)

    // Timetable
    @Query("SELECT * FROM timetable_slots ORDER BY periodNumber ASC")
    fun observeTimetableSlots(): Flow<List<TimetableSlotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetableSlot(slot: TimetableSlotEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetableSlots(slots: List<TimetableSlotEntity>)

    @Query("DELETE FROM timetable_slots WHERE id = :slotId")
    suspend fun deleteTimetableSlot(slotId: Int)

    // Assignments
    @Query("SELECT * FROM assignments ORDER BY timestamp DESC")
    fun observeAssignments(): Flow<List<AssignmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: AssignmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignments(assignments: List<AssignmentEntity>)

    @Update
    suspend fun updateAssignment(assignment: AssignmentEntity)

    @Query("DELETE FROM assignments WHERE id = :id")
    suspend fun deleteAssignment(id: Int)

    // Teacher-Student Messaging Portal
    @Query("SELECT * FROM chat_threads ORDER BY lastMessageTime DESC")
    fun observeChatThreads(): Flow<List<ChatThreadEntity>>

    @Query("SELECT * FROM chat_messages WHERE threadId = :threadId ORDER BY timestamp ASC")
    fun observeMessagesForThread(threadId: Int): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatThread(thread: ChatThreadEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatThreads(threads: List<ChatThreadEntity>)

    @Update
    suspend fun updateChatThread(thread: ChatThreadEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessages(messages: List<ChatMessageEntity>)

    @Query("SELECT * FROM chat_threads WHERE id = :threadId LIMIT 1")
    suspend fun getChatThreadById(threadId: Int): ChatThreadEntity?
}
