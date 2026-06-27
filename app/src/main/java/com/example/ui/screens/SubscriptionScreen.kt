package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.IPTVViewModel
import com.example.ui.components.ThreeDButton
import com.example.ui.components.ThreeDCheckoutCard

@Composable
fun SubscriptionScreen(
    viewModel: IPTVViewModel,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()
    var selectedPlan by remember { mutableStateOf("PRO") } // "PRO" or "ULTIMATE"
    var paymentCompleted by remember { mutableStateOf(false) }

    val planPrice = if (selectedPlan == "PRO") "€4.99 / muaj" else "€8.99 / muaj"
    val planPriceValue = if (selectedPlan == "PRO") "€4.99" else "€8.99"
    val planAccountsCount = if (selectedPlan == "PRO") "Deri në 2 Llogari" else "Llogari Pafund"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030712)) // Darkest deep space grey
    ) {
        // Main Scrollable Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(top = 70.dp, bottom = 40.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Visual Premium Banner Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0xFFFBBF24).copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_premium_subscription),
                    contentDescription = "Plan Premium Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Dark overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xFF030712).copy(alpha = 0.95f))
                            )
                        )
                )
                
                // Icon label
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Ylli",
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ZHBLLOKO PREMIUM",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Zgjidhni Planin Tuaj",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Tejkalo kufirin e 1 llogarie aktive IPTV dhe shijo vizualitetin superior 3D",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Plan Selection Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pro Plan Option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (selectedPlan == "PRO") Color(0xFF7C3AED).copy(alpha = 0.15f)
                            else Color(0xFF1F2937).copy(alpha = 0.4f)
                        )
                        .border(
                            width = 2.dp,
                            color = if (selectedPlan == "PRO") Color(0xFF7C3AED) else Color.Transparent,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { selectedPlan = "PRO" }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "PREMIUM PRO",
                            color = if (selectedPlan == "PRO") Color(0xFFC084FC) else Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "€4.99", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Text(text = "/ muaj", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Deri në 2 Llogari\nLista pafund",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Ultimate Plan Option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (selectedPlan == "ULTIMATE") Color(0xFF0D9488).copy(alpha = 0.15f)
                            else Color(0xFF1F2937).copy(alpha = 0.4f)
                        )
                        .border(
                            width = 2.dp,
                            color = if (selectedPlan == "ULTIMATE") Color(0xFF0D9488) else Color.Transparent,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { selectedPlan = "ULTIMATE" }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "ULTIMATE 3D",
                            color = if (selectedPlan == "ULTIMATE") Color(0xFF2DD4BF) else Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "€8.99", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Text(text = "/ muaj", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Llogari Pafund\nSuport VIP 24/7",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Interactive 3D Checkout Section
            if (!paymentCompleted) {
                Text(
                    text = "Lidhni Kartën e Pagesës",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                Text(
                    text = "Prekni kartën për ta kthyer (flipur) në 3D dhe parë CVV",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    textAlign = TextAlign.Start
                )

                // 3D Flappable Credit Card
                ThreeDCheckoutCard(
                    planPrice = planPriceValue
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Card benefits details list
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF111827))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BenefitRow(text = "Mundësi për të shtuar $planAccountsCount")
                    BenefitRow(text = "Mbështetje e plotë për M3U8, Xtream & Stalker")
                    BenefitRow(text = "Vizualizim Spektakolar 3D dhe efekte transicioni")
                    BenefitRow(text = "Pa reklama, server me shpejtësi maksimale")
                }

                Spacer(modifier = Modifier.height(28.dp))

                // 3D Pay Button
                ThreeDButton(
                    text = "PAGUAJ $planPriceValue",
                    onClick = {
                        paymentCompleted = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "pay_button",
                    color = if (selectedPlan == "PRO") Color(0xFF6D28D9) else Color(0xFF0F766E),
                    topColor = if (selectedPlan == "PRO") Color(0xFF8B5CF6) else Color(0xFF0D9488)
                )
            } else {
                // Success State Screen with elegant presentation
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF065F46).copy(alpha = 0.15f))
                        .border(1.dp, Color(0xFF059669).copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Sukses",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "BLERJA U KRYE ME SUKSES!",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Llogaria juaj u përmirësua në statusin Premium Pro. Tani mund të shtoni llogari të pakufizuara IPTV në sistem.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // 3D Back to Dashboard button
                    ThreeDButton(
                        text = "FILLONI TANI",
                        onClick = {
                            viewModel.purchasePremium()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "success_done_button",
                        color = Color(0xFF059669),
                        topColor = Color(0xFF10B981)
                    )
                }
            }
        }

        // Close Overlay Button
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 20.dp, end = 20.dp)
                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(50))
                .testTag("close_paywall_button")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Mbyll",
                tint = Color.White
            )
        }
    }
}

@Composable
fun BenefitRow(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Pikat",
            tint = Color(0xFF10B981),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 13.sp
        )
    }
}
