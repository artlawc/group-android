package com.groupa.domain.usecase

import com.groupa.domain.model.ChatMessage
import com.groupa.domain.model.ChatThread
import com.groupa.domain.model.GroupMessage
import com.groupa.domain.model.Contact
import com.groupa.domain.model.StockInfo
import com.groupa.domain.model.User
import com.groupa.domain.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserUseCase @Inject constructor(private val repository: GroupRepository) {
    operator fun invoke(): Flow<User> = repository.user
}

class GetMessagesUseCase @Inject constructor(private val repository: GroupRepository) {
    operator fun invoke(): Flow<List<GroupMessage>> = repository.messages
}

class GetChatsUseCase @Inject constructor(private val repository: GroupRepository) {
    operator fun invoke(): Flow<List<ChatThread>> = repository.chats
}

class GetChatMessagesUseCase @Inject constructor(private val repository: GroupRepository) {
    operator fun invoke(): Flow<List<ChatMessage>> = repository.chatMessages
}

class GetContactsUseCase @Inject constructor(private val repository: GroupRepository) {
    operator fun invoke(): Flow<List<Contact>> = repository.contacts
}

class AddContactUseCase @Inject constructor(private val repository: GroupRepository) {
    operator fun invoke(name: String, phone: String): Contact =
        repository.addContact(name, phone)
}

class GetStocksUseCase @Inject constructor(private val repository: GroupRepository) {
    operator fun invoke(): Flow<List<StockInfo>> = repository.stocks
}

class SignInUseCase @Inject constructor(private val repository: GroupRepository) {
    operator fun invoke(email: String, password: String): User = repository.signIn(email, password)
}

class SignInWithProviderUseCase @Inject constructor(private val repository: GroupRepository) {
    operator fun invoke(provider: String): User = repository.signInWithProvider(provider)
}

class UpdateProfileUseCase @Inject constructor(private val repository: GroupRepository) {
    operator fun invoke(name: String, email: String, phone: String): User =
        repository.updateProfile(name, email, phone)
}

class UpdateAvatarUseCase @Inject constructor(private val repository: GroupRepository) {
    operator fun invoke(avatarUri: String): User = repository.updateAvatar(avatarUri)
}

class ChangePasswordUseCase @Inject constructor(private val repository: GroupRepository) {
    operator fun invoke(currentPassword: String, newPassword: String): Boolean =
        repository.changePassword(currentPassword, newPassword)
}

class SignOutUseCase @Inject constructor(private val repository: GroupRepository) {
    operator fun invoke() = repository.signOut()
}

class MarkAllMessagesReadUseCase @Inject constructor(private val repository: GroupRepository) {
    operator fun invoke() = repository.markAllMessagesRead()
}

class MarkMessageReadUseCase @Inject constructor(private val repository: GroupRepository) {
    operator fun invoke(messageId: String) = repository.markMessageRead(messageId)
}

class AddMessageUseCase @Inject constructor(private val repository: GroupRepository) {
    operator fun invoke(message: GroupMessage) = repository.addMessage(message)
}

class DeleteMessagesUseCase @Inject constructor(private val repository: GroupRepository) {
    operator fun invoke(messageIds: Set<String>) = repository.deleteMessages(messageIds)
}

class SendChatMessageUseCase @Inject constructor(private val repository: GroupRepository) {
    operator fun invoke(text: String) = repository.sendChatMessage(text)
}
