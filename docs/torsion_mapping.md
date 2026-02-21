# Torsion Shaft Calculator - Mapeamento de IDs

| ID | Variável | Conversão/Fórmula |
|---|---|---|
| `input_torsion_force` + `input_torsion_force_unit` | `forceValue`, `forceUnit` | `F_N = F` (N) ou `F_N = F * 9.80665` (kgf) |
| `input_torsion_arm` + `input_torsion_arm_unit` | `armValue`, `armUnit` | `r_m = r/1000` (mm) ou `r_m = r` (m) |
| `input_torsion_phi` | `phiDeg` | `phi_rad = phi_deg * π/180` |
| `input_torsion_outer_diameter` + unidade | `outerDiameterValue`, `outerDiameterUnit` | `D_m = D/1000` (mm) ou `D` (m) |
| `input_torsion_is_hollow` | `isHollow` | Se ligado, usa `d` |
| `input_torsion_inner_diameter` + unidade | `innerDiameterValue`, `innerDiameterUnit` | `d_m = d/1000` (mm) ou `d` (m), validar `d < D` |
| `input_torsion_length` + unidade | `lengthValue`, `lengthUnit` | `L_m = L/1000` (mm) ou `L` (m) |
| `input_torsion_g` + `input_torsion_g_unit` | `shearModulusValue`, `shearModulusUnit` | `G_Pa = G*1e9` (GPa) ou `G*1e6` (MPa) |
| `input_torsion_tau_y` + unidade | `shearYieldValue` | `tauY_Pa = tauY*1e6` (MPa) |
| `input_torsion_fs` | `fs` | `tau_adm = tauY_Pa / FS` |
| `output_torsion_torque` | `torqueNm` | `T = F_N * r_m * sin(phi_rad)` |
| `output_torsion_j` | `polarMomentM4` | `J = π*D^4/32` (maciço) ou `π*(D^4-d^4)/32` (oco) |
| `output_torsion_tau` | `tauMpa` | `tau = (T*c)/J`, `c=D/2`, depois `Pa->MPa` |
| `output_torsion_theta` | `thetaDeg` | `theta_rad = (T*L)/(J*G)`, `theta_deg = theta_rad*180/π` |
| `output_torsion_k` | `torsionalRigidity` | `k = T/theta_rad` |
| `output_torsion_status` | `status` | `OK` se `tau <= tauY/FS`, senão `FALHOU` |
| `output_torsion_fs_obt` | `fsObt` | `FS_obt = tauY/tau` |
