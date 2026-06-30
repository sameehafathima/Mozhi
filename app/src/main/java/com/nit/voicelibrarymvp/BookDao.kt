package com.nit.voicelibrarymvp

import androidx.room.*

@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book)

    @Delete
    suspend fun deleteBook(book: Book)

    @Query("SELECT * FROM books")
    suspend fun getAllBooks(): List<Book>

    @Query("""
        SELECT * FROM books 
        WHERE title LIKE '%' || :search || '%' 
        OR author LIKE '%' || :search || '%' 
        OR location LIKE '%' || :search || '%'
        OR accessionNumber LIKE '%' || :search || '%'
    """)
    suspend fun searchBooks(search: String): List<Book>

    @Query("SELECT * FROM books WHERE accessionNumber = :acc")
    suspend fun getBookByAccession(acc: String): Book?

    @Query("SELECT * FROM books WHERE TRIM(title) = TRIM(:title) COLLATE NOCASE LIMIT 1")
    suspend fun findBookByTitle(title: String): Book?

    // Note: incrementCopyCount is less relevant if every book is a unique record, 
    // but keeping for backward compatibility or future use.
    @Query("UPDATE books SET numberOfCopies = numberOfCopies + :increment WHERE accessionNumber = :id")
    suspend fun incrementCopyCount(id: String, increment: Int)

    @Query("DELETE FROM books WHERE libraryId = :libId")
    suspend fun deleteByLibraryId(libId: String)
}

@Dao
interface ReviewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review)

    @Query("SELECT * FROM reviews WHERE bookId = :bookRemoteId ORDER BY timestamp DESC")
    suspend fun getReviewsForRemoteBook(bookRemoteId: String): List<Review>
}

@Dao
interface BorrowRequestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: BorrowRequest)

    @Delete
    suspend fun deleteRequest(request: BorrowRequest)

    @Query("SELECT * FROM borrow_requests WHERE libraryId = :libId")
    suspend fun getRequestsForLibrary(libId: String): List<BorrowRequest>

    @Query("SELECT * FROM borrow_requests WHERE userUid = :uid")
    suspend fun getRequestsForUser(uid: String): List<BorrowRequest>

    @Query("DELETE FROM borrow_requests WHERE libraryId = :libId")
    suspend fun deleteByLibraryId(libId: String)
}
