# ═══════════════════════════════════════════════════════════════
# AI-Powered LMS — End-to-End API Integration Verification Suite
# Run: powershell -ExecutionPolicy Bypass -File .\test-api.ps1
# Prerequisites: Application running on http://localhost:8080
# ═══════════════════════════════════════════════════════════════

Add-Type -AssemblyName System.Net.Http
$baseUrl = "http://localhost:8080/api/v1"
$passed = 0
$failed = 0

function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Url,
        [string]$Body = $null,
        [string]$Token = $null,
        [int]$ExpectedStatus = 200,
        [switch]$ExpectError
    )

    Write-Host ""
    Write-Host "--- TEST: $Name ---" -ForegroundColor Cyan

    try {
        $headers = @{}
        if ($Token) {
            $headers.Add("Authorization", "Bearer $Token")
        }

        $params = @{
            Uri     = $Url
            Method  = $Method
            Headers = $headers
        }

        if ($Body) {
            $params.ContentType = "application/json"
            $params.Body = [System.Text.Encoding]::UTF8.GetBytes($Body)
        }

        $response = Invoke-RestMethod @params
        $json = $response | ConvertTo-Json -Depth 5

        if ($ExpectError) {
            Write-Host "  FAIL - Expected error but got success" -ForegroundColor Red
            $script:failed++
        } else {
            Write-Host "  PASS" -ForegroundColor Green
            Write-Host $json
            $script:passed++
        }

        return $response
    }
    catch {
        $statusCode = 0
        if ($_.Exception.Response) {
            $statusCode = [int]$_.Exception.Response.StatusCode
        }

        if ($ExpectError) {
            Write-Host "  PASS - Got expected error (HTTP $statusCode)" -ForegroundColor Green
            $script:passed++

            try {
                $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
                $errorBody = $reader.ReadToEnd()
                Write-Host $errorBody
            } catch {}
        } else {
            Write-Host "  FAIL - HTTP $statusCode : $($_.Exception.Message)" -ForegroundColor Red
            $script:failed++
        }

        return $null
    }
}

Write-Host ""
Write-Host "==================================================" -ForegroundColor Yellow
Write-Host "  AI-Powered LMS - Sprint 1-12 Integration Suite"  -ForegroundColor Yellow
Write-Host "==================================================" -ForegroundColor Yellow

# ════════════════════ 1. Login Admin ════════════════════

$loginAdminBody = @{email="admin@lms.com"; password="admin12345"} | ConvertTo-Json
$adminAuth = Test-Endpoint -Name "Login as Admin" -Method POST -Url "$baseUrl/auth/login" -Body $loginAdminBody
$adminToken = $adminAuth.data.accessToken

# ════════════════════ 2. Category Operations (Admin Only) ════════════════════

$rootCatBody = @{name="Development"; description="Software programming and development"} | ConvertTo-Json
$rootCat = Test-Endpoint -Name "Create Root Category (Admin)" -Method POST -Url "$baseUrl/categories" -Body $rootCatBody -Token $adminToken
$rootCatId = $rootCat.data.id

$childCatBody = @{name="Web Development"; description="HTML, CSS, JavaScript, Spring Boot, React"; parentId=$rootCatId} | ConvertTo-Json
$childCat = Test-Endpoint -Name "Create Child Category (Admin)" -Method POST -Url "$baseUrl/categories" -Body $childCatBody -Token $adminToken
$childCatId = $childCat.data.id

# ════════════════════ 3. Create Instructor User ════════════════════

$createInstBody = @{firstName="John"; lastName="Doe"; email="instructor@lms.com"; password="password123"; role="INSTRUCTOR"} | ConvertTo-Json
$instructorUser = Test-Endpoint -Name "Create Instructor Account via Admin" -Method POST -Url "$baseUrl/users" -Body $createInstBody -Token $adminToken

# ════════════════════ 4. Login Instructor ════════════════════

$loginInstBody = @{email="instructor@lms.com"; password="password123"} | ConvertTo-Json
$instAuth = Test-Endpoint -Name "Login as Instructor" -Method POST -Url "$baseUrl/auth/login" -Body $loginInstBody
$instToken = $instAuth.data.accessToken

# ════════════════════ 5. Course CRUD Operations (Instructor) ════════════════════

$courseBody = @{
    title = "Mastering Spring Boot 3"
    shortDescription = "Build production-ready applications with Spring Boot"
    description = "A comprehensive deep dive into enterprise-grade Spring Boot architecture, database tuning, and security."
    thumbnailUrl = "http://example.com/spring.png"
    price = 99.99
    difficulty = "INTERMEDIATE"
    status = "DRAFT"
    language = "English"
    categoryId = $childCatId
} | ConvertTo-Json

$course = Test-Endpoint -Name "Create Course (Instructor)" -Method POST -Url "$baseUrl/courses" -Body $courseBody -Token $instToken
$courseId = $course.data.id

# ════════════════════ 6. Student Signup & Login ════════════════════

$signupBody = @{firstName="Student"; lastName="User"; email="student@lms.com"; password="password123"} | ConvertTo-Json
$studentUser = Test-Endpoint -Name "Signup as Student (Public)" -Method POST -Url "$baseUrl/auth/signup" -Body $signupBody

$loginStudBody = @{email="student@lms.com"; password="password123"} | ConvertTo-Json
$studentAuth = Test-Endpoint -Name "Login as Student" -Method POST -Url "$baseUrl/auth/login" -Body $loginStudBody
$studentToken = $studentAuth.data.accessToken

# ════════════════════ 7. File Storage Verification ════════════════════

# Build valid text file
$txtPath = "temp.txt"
Set-Content -Path $txtPath -Value "Hello from student submission!"

Write-Host ""
Write-Host "--- TEST: Student uploads valid text file ---" -ForegroundColor Cyan

$fileBytes = [System.IO.File]::ReadAllBytes($txtPath)
$fileContent = [System.Net.Http.ByteArrayContent]::new($fileBytes)
$fileContent.Headers.ContentType = [System.Net.Http.Headers.MediaTypeHeaderValue]::Parse("text/plain")

$multipartContent = [System.Net.Http.MultipartFormDataContent]::new()
$multipartContent.Add($fileContent, "file", "temp.txt")

$client = [System.Net.Http.HttpClient]::new()
$client.DefaultRequestHeaders.Add("Authorization", "Bearer $studentToken")

$responseTask = $client.PostAsync("$baseUrl/files/upload", $multipartContent)
$responseTask.Wait()
$uploadResponse = $responseTask.Result
$uploadStatus = [int]$uploadResponse.StatusCode
$uploadBodyStr = $uploadResponse.Content.ReadAsStringAsync().Result

if ($uploadStatus -eq 200) {
    Write-Host "  PASS" -ForegroundColor Green
    Write-Host $uploadBodyStr
    $script:passed++

    $uploadJson = $uploadBodyStr | ConvertFrom-Json
    $uploadedUrl = $uploadJson.data.fileUrl
    $uploadedName = $uploadJson.data.fileName

    # Test downloading/serving the file publicly (WITHOUT TOKEN)
    Write-Host ""
    Write-Host "--- TEST: Serve static file publicly (GET without token) ---" -ForegroundColor Cyan
    try {
        $servedContent = Invoke-RestMethod -Uri $uploadedUrl -Method GET
        Write-Host "  Content: $servedContent"
        if ($servedContent.Trim() -eq "Hello from student submission!") {
            Write-Host "  PASS - Public serve content matched successfully!" -ForegroundColor Green
            $script:passed++
        } else {
            Write-Host "  FAIL - Content mismatch!" -ForegroundColor Red
            $script:failed++
        }
    }
    catch {
        Write-Host "  FAIL - Public download failed: $($_.Exception.Message)" -ForegroundColor Red
        $script:failed++
    }
} else {
    Write-Host "  FAIL - Upload failed: $uploadStatus - $uploadBodyStr" -ForegroundColor Red
    $script:failed++
}

# Clean up temp file
Remove-Item -Path $txtPath -ErrorAction SilentlyContinue

# ════════════════════ 8. Upload Invalid File Extension ════════════════════

$exePath = "malicious.exe"
Set-Content -Path $exePath -Value "malicious binary content"

Write-Host ""
Write-Host "--- TEST: Upload file with invalid extension (Expect 400) ---" -ForegroundColor Cyan

$exeBytes = [System.IO.File]::ReadAllBytes($exePath)
$exeContent = [System.Net.Http.ByteArrayContent]::new($exeBytes)
$exeContent.Headers.ContentType = [System.Net.Http.Headers.MediaTypeHeaderValue]::Parse("application/octet-stream")

$multipartContent2 = [System.Net.Http.MultipartFormDataContent]::new()
$multipartContent2.Add($exeContent, "file", "malicious.exe")

$client2 = [System.Net.Http.HttpClient]::new()
$client2.DefaultRequestHeaders.Add("Authorization", "Bearer $studentToken")

$responseTask2 = $client2.PostAsync("$baseUrl/files/upload", $multipartContent2)
$responseTask2.Wait()
$uploadResponse2 = $responseTask2.Result
$uploadStatus2 = [int]$uploadResponse2.StatusCode
$uploadBodyStr2 = $uploadResponse2.Content.ReadAsStringAsync().Result

if ($uploadStatus2 -eq 400 -or $uploadStatus2 -eq 422) {
    Write-Host "  PASS - Upload blocked with code $uploadStatus2" -ForegroundColor Green
    Write-Host $uploadBodyStr2
    $script:passed++
} else {
    Write-Host "  FAIL - Upload should have failed (code 400/422 expected, got $uploadStatus2)" -ForegroundColor Red
    $script:failed++
}

Remove-Item -Path $exePath -ErrorAction SilentlyContinue

# ════════════════════ 9. Course Enrollment ════════════════════

$enroll = Test-Endpoint -Name "Student enrolls in Course" -Method POST -Url "$baseUrl/courses/$courseId/enroll" -Token $studentToken

# ════════════════════ 10. Verify Enrollment Notification (Instructor) ════════════════════

$instNotifications = Test-Endpoint -Name "Instructor retrieves Notification History" -Method GET -Url "$baseUrl/notifications" -Token $instToken
$enrollNotification = $instNotifications.data.content[0]

Write-Host "  Title: $($enrollNotification.title), Msg: $($enrollNotification.message), Type: $($enrollNotification.type)"

if ($enrollNotification.title -eq "New Student Enrolled" -and $enrollNotification.type -eq "ENROLLMENT") {
    Write-Host "  PASS - Enrollment notification verified successfully!" -ForegroundColor Green
    $script:passed++
} else {
    Write-Host "  FAIL - Enrollment notification mismatch!" -ForegroundColor Red
    $script:failed++
}

# ════════════════════ 11. Create & Grade Assignment (Trigger Notification) ════════════════════

# Create Assignment
$assignmentBody = @{
    title = "Spring Boot Basics"
    description = "Complete the workspace questions"
    instructions = "Upload solution text"
    maxScore = 100
    dueDate = "2026-12-31T23:59:59"
} | ConvertTo-Json

$assignment = Test-Endpoint -Name "Instructor creates Assignment" -Method POST -Url "$baseUrl/courses/$courseId/assignments" -Body $assignmentBody -Token $instToken
$assignmentId = $assignment.data.id

# Submit Assignment
$submitBody = @{
    content = "My complete solution code."
    fileUrl = "http://localhost:8080/api/v1/files/submission.txt"
} | ConvertTo-Json

$submission = Test-Endpoint -Name "Student submits Assignment" -Method POST -Url "$baseUrl/assignments/$assignmentId/submissions" -Body $submitBody -Token $studentToken
$submissionId = $submission.data.id

# Grade Assignment
$gradeBody = @{
    grade = 95
    feedback = "Outstanding solution!"
} | ConvertTo-Json

Test-Endpoint -Name "Instructor grades Assignment" -Method PUT -Url "$baseUrl/assignments/$assignmentId/submissions/$submissionId/grade" -Body $gradeBody -Token $instToken

# ════════════════════ 12. Create & Submit Quiz (Trigger Notification) ════════════════════

# Create Quiz
$quizBody = @{
    title = "Core Architecture Quiz"
    description = "Test your skills on Spring container and beans lifecycle"
    timeLimit = 15
    passingScore = 80.0
    maxAttempts = 3
    shuffleQuestions = $false
} | ConvertTo-Json

$quiz = Test-Endpoint -Name "Instructor creates Quiz" -Method POST -Url "$baseUrl/courses/$courseId/quizzes" -Body $quizBody -Token $instToken
$quizId = $quiz.data.id

# Add Question
$questionBody = @(
    @{
        questionText = "Which annotation marks Spring configuration classes?"
        type = "SHORT_ANSWER"
        correctAnswer = "@Configuration"
        explanation = "Use @Configuration to declare bean definitions"
        points = 10
        sortOrder = 1
    }
) | ConvertTo-Json

Test-Endpoint -Name "Instructor adds Question to Quiz" -Method POST -Url "$baseUrl/quizzes/$quizId/questions" -Body $questionBody -Token $instToken

# Start Attempt
$attempt = Test-Endpoint -Name "Student starts Quiz Attempt" -Method POST -Url "$baseUrl/quizzes/$quizId/attempts" -Token $studentToken
$attemptId = $attempt.data.attemptId
$qId = $attempt.data.questions[0].id

# Submit Attempt
$quizSubmitBody = @{
    answers = @(
        @{
            questionId = $qId
            selectedAnswer = "@Configuration"
        }
    )
} | ConvertTo-Json

Test-Endpoint -Name "Student submits Quiz Attempt" -Method POST -Url "$baseUrl/quizzes/$quizId/attempts/$attemptId/submit" -Body $quizSubmitBody -Token $studentToken

# ════════════════════ 13. Verify Student Notifications ════════════════════

$studentUnread = Test-Endpoint -Name "Student checks Unread Notification Count" -Method GET -Url "$baseUrl/notifications/unread-count" -Token $studentToken
$unreadCount = $studentUnread.data

Write-Host "  Unread count: $unreadCount"

if ($unreadCount -eq 2) {
    Write-Host "  PASS - Student unread count verified!" -ForegroundColor Green
    $script:passed++
} else {
    Write-Host "  FAIL - Student unread count should be 2!" -ForegroundColor Red
    $script:failed++
}

# Fetch student notifications history
$studentNotifications = Test-Endpoint -Name "Student retrieves Notification History" -Method GET -Url "$baseUrl/notifications" -Token $studentToken
$firstNotification = $studentNotifications.data.content[0]
$secondNotification = $studentNotifications.data.content[1]

Write-Host "  1st: Title: $($firstNotification.title), Msg: $($firstNotification.message), Type: $($firstNotification.type)"
Write-Host "  2nd: Title: $($secondNotification.title), Msg: $($secondNotification.message), Type: $($secondNotification.type)"

# Mark specific notification as read
$notificationToRead = $firstNotification.id
Test-Endpoint -Name "Student marks first notification read" -Method PUT -Url "$baseUrl/notifications/$notificationToRead/read" -Token $studentToken

# Check unread count decreased
$studentUnread2 = Test-Endpoint -Name "Student checks Unread Count after single read update" -Method GET -Url "$baseUrl/notifications/unread-count" -Token $studentToken
$unreadCount2 = $studentUnread2.data

Write-Host "  New unread count: $unreadCount2"

if ($unreadCount2 -eq 1) {
    Write-Host "  PASS - Unread count decreased successfully!" -ForegroundColor Green
    $script:passed++
} else {
    Write-Host "  FAIL - Unread count should have decreased to 1!" -ForegroundColor Red
    $script:failed++
}

# Mark all read
Test-Endpoint -Name "Student marks all notifications as read" -Method PUT -Url "$baseUrl/notifications/read-all" -Token $studentToken

# Check unread count is 0
$studentUnread3 = Test-Endpoint -Name "Student checks Unread Count after mark-all-read" -Method GET -Url "$baseUrl/notifications/unread-count" -Token $studentToken
$unreadCount3 = $studentUnread3.data

Write-Host "  Final unread count: $unreadCount3"

if ($unreadCount3 -eq 0) {
    Write-Host "  PASS - All read verification complete!" -ForegroundColor Green
    $script:passed++
} else {
    Write-Host "  FAIL - Unread count is not zero!" -ForegroundColor Red
    $script:failed++
}

# ════════════════════ SUMMARY ════════════════════

Write-Host ""
Write-Host "=============================================" -ForegroundColor Yellow
Write-Host "  RESULTS: Passed=$passed  Failed=$failed"     -ForegroundColor $(if ($failed -eq 0) {"Green"} else {"Red"})
Write-Host "=============================================" -ForegroundColor Yellow
