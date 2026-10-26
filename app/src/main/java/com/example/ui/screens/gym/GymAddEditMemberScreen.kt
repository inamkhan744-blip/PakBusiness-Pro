package com.example.ui.screens.gym

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GymMemberEntity
import com.example.ui.theme.PakEmeraldContainer
import com.example.ui.theme.PakEmeraldDark
import com.example.ui.theme.PakEmeraldPrimary
import com.example.ui.theme.PakGoldContainer
import com.example.ui.theme.PakGoldSecondary

data class GymPlanOption(
    val name: String,
    val durationDays: Int,
    val defaultPricePkr: Double
)

val GYM_PLANS = listOf(
    GymPlanOption("Monthly Standard", 30, 3500.0),
    GymPlanOption("Quarterly (3 Mos)", 90, 9500.0),
    GymPlanOption("Semi-Annual (6 Mos)", 180, 18000.0),
    GymPlanOption("Annual VIP", 365, 32000.0),
    GymPlanOption("CrossFit & Cardio", 30, 5500.0)
)

val GENDER_OPTIONS = listOf("Male", "Female", "Other")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymAddEditMemberScreen(
    memberToEdit: GymMemberEntity?,
    onSave: (name: String, phone: String, gender: String, plan: String, startDate: Long, durationDays: Int, amount: Double) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    onSaveFull: ((
        name: String,
        phone: String,
        gender: String,
        plan: String,
        startDate: Long,
        durationDays: Int,
        amount: Double,
        admissionFee: Double,
        pendingDue: Double,
        fitnessGoal: String,
        workoutPlan: String,
        dietPlan: String,
        lockerNumber: String
    ) -> Unit)? = null
) {
    val isEditMode = memberToEdit != null

    var name by remember { mutableStateOf(memberToEdit?.name ?: "") }
    var phone by remember { mutableStateOf(memberToEdit?.phone ?: "") }
    var selectedGender by remember { mutableStateOf(memberToEdit?.gender ?: "Male") }
    var selectedPlan by remember { mutableStateOf(memberToEdit?.plan ?: GYM_PLANS[0].name) }
    var durationDays by remember { mutableIntStateOf(memberToEdit?.durationDays ?: 30) }
    var startDate by remember { mutableLongStateOf(memberToEdit?.startDate ?: System.currentTimeMillis()) }
    var amountText by remember {
        mutableStateOf(
            memberToEdit?.amountPkr?.let { "%.0f".format(it) } ?: "3500"
        )
    }

    // Advanced Fitness & Facility Fields
    var lockerNumber by remember { mutableStateOf(memberToEdit?.lockerNumber ?: "") }
    var admissionFeeText by remember { mutableStateOf(memberToEdit?.let { "%.0f".format(it.admissionFeePkr) } ?: "0") }
    var pendingDueText by remember { mutableStateOf(memberToEdit?.let { "%.0f".format(it.pendingDuePkr) } ?: "0") }
    var fitnessGoal by remember { mutableStateOf(memberToEdit?.fitnessGoal ?: "Muscle Hypertrophy") }
    var workoutPlan by remember { mutableStateOf(memberToEdit?.workoutPlan ?: "") }
    var dietPlan by remember { mutableStateOf(memberToEdit?.dietPlan ?: "") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    val calculatedExpiry = startDate + (durationDays.toLong() * 24L * 60L * 60L * 1000L)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Edit Member" else "Add New Member",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize().testTag("gym_add_edit_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header Info Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = PakEmeraldContainer.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(PakEmeraldPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isEditMode) "Updating Member Record" else "Register Member for Gym",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = PakEmeraldPrimary
                        )
                        Text(
                            text = "Fill in membership parameters and save locally offline.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Section 1: Member Personal Info
            Text(
                text = "1. Personal Information",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Name
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (it.isNotBlank()) nameError = null
                },
                label = { Text("Full Name *") },
                placeholder = { Text("e.g. Asad Qureshi") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PakEmeraldPrimary) },
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gym_member_name_input")
            )

            // Phone
            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it
                    if (it.isNotBlank()) phoneError = null
                },
                label = { Text("Phone Number *") },
                placeholder = { Text("e.g. +92 300 1234567") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = PakEmeraldPrimary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = phoneError != null,
                supportingText = phoneError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gym_member_phone_input")
            )

            // Gender (Segmented chips)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Gender *",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GENDER_OPTIONS.forEach { gender ->
                        val isSelected = selectedGender == gender
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) PakEmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedGender = gender }
                                .testTag("gender_chip_$gender")
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = gender,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Section 2: Membership Plan & Subscription
            Text(
                text = "2. Membership Plan",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GYM_PLANS.forEach { planOption ->
                    val isSelected = selectedPlan == planOption.name
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) PakEmeraldContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, PakEmeraldPrimary) else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedPlan = planOption.name
                                durationDays = planOption.durationDays
                                if (!isEditMode || amountText.isBlank()) {
                                    amountText = "%.0f".format(planOption.defaultPricePkr)
                                }
                            }
                            .testTag("plan_option_${planOption.durationDays}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .background(
                                            if (isSelected) PakEmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                                Column {
                                    Text(
                                        text = planOption.name,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${planOption.durationDays} Days Duration",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Text(
                                text = "₨ ${"%,.0f".format(planOption.defaultPricePkr)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = PakEmeraldPrimary
                            )
                        }
                    }
                }
            }

            // Section 3: Start Date & Amount (PKR)
            Text(
                text = "3. Timeline & Billing",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Start Date Display & Expiry summary
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = PakEmeraldPrimary, modifier = Modifier.size(18.dp))
                            Text(text = "Start Date:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        Text(
                            text = GymDateUtils.formatDate(startDate),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PakEmeraldPrimary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Calculated Expiry Date:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text(
                            text = GymDateUtils.formatDate(calculatedExpiry),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828)
                        )
                    }

                    // Quick start date adjustments
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        FilterChip(
                            selected = true,
                            onClick = { startDate = System.currentTimeMillis() },
                            label = { Text("Set to Today") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PakEmeraldContainer,
                                selectedLabelColor = PakEmeraldPrimary
                            )
                        )
                        FilterChip(
                            selected = false,
                            onClick = { startDate = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000) },
                            label = { Text("1 Week Ago") }
                        )
                    }
                }
            }

            // Amount Paid (PKR)
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount Paid (PKR) *") },
                placeholder = { Text("e.g. 3500") },
                leadingIcon = {
                    Text(
                        text = "₨",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = PakEmeraldPrimary,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                },
                trailingIcon = {
                    Text(
                        text = "PKR",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gym_member_amount_input")
            )

            // Section 4: Facility, Locker & Fitness Profile (Optional)
            Text(
                text = "4. Facility, Locker & Goals (Optional)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Locker Number
                OutlinedTextField(
                    value = lockerNumber,
                    onValueChange = { lockerNumber = it },
                    label = { Text("Locker Number") },
                    placeholder = { Text("e.g. L-12") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PakEmeraldPrimary) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("gym_member_locker_input")
                )

                // Admission Fee
                OutlinedTextField(
                    value = admissionFeeText,
                    onValueChange = { admissionFeeText = it },
                    label = { Text("Admission Fee (PKR)") },
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("gym_member_admission_fee_input")
                )
            }

            // Pending Dues (PKR)
            OutlinedTextField(
                value = pendingDueText,
                onValueChange = { pendingDueText = it },
                label = { Text("Pending Due / Balance (PKR)") },
                placeholder = { Text("0") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gym_member_due_input")
            )

            // Fitness Goal selection
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Primary Fitness Goal",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val goals = listOf("Muscle Hypertrophy", "Weight Loss", "Cardio / Stamina", "Strength & Power", "General Fitness")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    goals.take(3).forEach { goal ->
                        val isSelected = fitnessGoal == goal
                        FilterChip(
                            selected = isSelected,
                            onClick = { fitnessGoal = goal },
                            label = { Text(goal, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PakEmeraldContainer,
                                selectedLabelColor = PakEmeraldPrimary
                            )
                        )
                    }
                }
            }

            // Workout & Diet Plan notes
            OutlinedTextField(
                value = workoutPlan,
                onValueChange = { workoutPlan = it },
                label = { Text("Workout Split / Routine Notes") },
                placeholder = { Text("e.g. Mon: Chest, Tue: Back, Wed: Legs, Thu: Shoulders") },
                leadingIcon = { Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = PakEmeraldPrimary) },
                maxLines = 2,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gym_member_workout_input")
            )

            OutlinedTextField(
                value = dietPlan,
                onValueChange = { dietPlan = it },
                label = { Text("Diet / Nutrition Recommendation Notes") },
                placeholder = { Text("e.g. 150g Protein daily, 4 Eggs, Chicken breast, Oats, Whey") },
                leadingIcon = { Icon(Icons.Default.Restaurant, contentDescription = null, tint = PakEmeraldPrimary) },
                maxLines = 2,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gym_member_diet_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        var hasError = false
                        if (name.isBlank()) {
                            nameError = "Member name is required"
                            hasError = true
                        }
                        if (phone.isBlank()) {
                            phoneError = "Phone number is required"
                            hasError = true
                        }

                        if (!hasError) {
                            val amount = amountText.toDoubleOrNull() ?: 3500.0
                            val admFee = admissionFeeText.toDoubleOrNull() ?: 0.0
                            val dueAmount = pendingDueText.toDoubleOrNull() ?: 0.0

                            if (onSaveFull != null) {
                                onSaveFull(
                                    name.trim(),
                                    phone.trim(),
                                    selectedGender,
                                    selectedPlan,
                                    startDate,
                                    durationDays,
                                    amount,
                                    admFee,
                                    dueAmount,
                                    fitnessGoal,
                                    workoutPlan,
                                    dietPlan,
                                    lockerNumber
                                )
                            } else {
                                onSave(
                                    name.trim(),
                                    phone.trim(),
                                    selectedGender,
                                    selectedPlan,
                                    startDate,
                                    durationDays,
                                    amount
                                )
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PakEmeraldPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_save_gym_member")
                ) {
                    Text(if (isEditMode) "Save Changes" else "Register Member", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
