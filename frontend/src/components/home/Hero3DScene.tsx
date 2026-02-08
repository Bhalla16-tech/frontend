import { Canvas, useFrame } from '@react-three/fiber';
import { Float, MeshDistortMaterial, Sphere, Torus, Box, RoundedBox } from '@react-three/drei';
import { useRef, useMemo } from 'react';
import * as THREE from 'three';

// Floating document/resume shape
function FloatingDocument({ position, rotation, delay }: { position: [number, number, number]; rotation: [number, number, number]; delay: number }) {
  const meshRef = useRef<THREE.Mesh>(null);
  
  useFrame((state) => {
    if (meshRef.current) {
      meshRef.current.rotation.y = Math.sin(state.clock.elapsedTime * 0.5 + delay) * 0.1;
      meshRef.current.rotation.x = Math.cos(state.clock.elapsedTime * 0.3 + delay) * 0.05;
      meshRef.current.position.y = position[1] + Math.sin(state.clock.elapsedTime * 0.8 + delay) * 0.15;
    }
  });

  return (
    <RoundedBox 
      ref={meshRef}
      args={[0.8, 1.1, 0.05]} 
      radius={0.02} 
      position={position}
      rotation={rotation}
    >
      <meshStandardMaterial 
        color="#ffd700" 
        metalness={0.3} 
        roughness={0.4}
        transparent
        opacity={0.15}
      />
    </RoundedBox>
  );
}

// Orbiting ring representing career path
function CareerRing({ radius, speed, yOffset }: { radius: number; speed: number; yOffset: number }) {
  const ringRef = useRef<THREE.Mesh>(null);
  
  useFrame((state) => {
    if (ringRef.current) {
      ringRef.current.rotation.z = state.clock.elapsedTime * speed;
      ringRef.current.rotation.x = Math.PI / 2 + Math.sin(state.clock.elapsedTime * 0.5) * 0.1;
    }
  });

  return (
    <Torus
      ref={ringRef}
      args={[radius, 0.015, 16, 100]}
      position={[0, yOffset, 0]}
    >
      <meshStandardMaterial 
        color="#ffc800" 
        metalness={0.8} 
        roughness={0.2}
        transparent
        opacity={0.4}
      />
    </Torus>
  );
}

// Central glowing orb representing opportunity/success
function CentralOrb() {
  const meshRef = useRef<THREE.Mesh>(null);
  
  useFrame((state) => {
    if (meshRef.current) {
      meshRef.current.scale.setScalar(1 + Math.sin(state.clock.elapsedTime * 2) * 0.05);
    }
  });

  return (
    <Float speed={2} rotationIntensity={0.5} floatIntensity={0.5}>
      <Sphere ref={meshRef} args={[0.6, 64, 64]} position={[0, 0, 0]}>
        <MeshDistortMaterial
          color="#ffb800"
          metalness={0.5}
          roughness={0.2}
          distort={0.3}
          speed={2}
          transparent
          opacity={0.25}
        />
      </Sphere>
    </Float>
  );
}

// Small floating particles representing achievements
function Particles() {
  const particlesRef = useRef<THREE.Points>(null);
  
  const particlePositions = useMemo(() => {
    const positions = new Float32Array(60 * 3);
    for (let i = 0; i < 60; i++) {
      const theta = Math.random() * Math.PI * 2;
      const phi = Math.acos(Math.random() * 2 - 1);
      const radius = 1.5 + Math.random() * 1.5;
      
      positions[i * 3] = radius * Math.sin(phi) * Math.cos(theta);
      positions[i * 3 + 1] = radius * Math.sin(phi) * Math.sin(theta);
      positions[i * 3 + 2] = radius * Math.cos(phi);
    }
    return positions;
  }, []);

  useFrame((state) => {
    if (particlesRef.current) {
      particlesRef.current.rotation.y = state.clock.elapsedTime * 0.1;
      particlesRef.current.rotation.x = Math.sin(state.clock.elapsedTime * 0.15) * 0.1;
    }
  });

  return (
    <points ref={particlesRef}>
      <bufferGeometry>
        <bufferAttribute
          attach="attributes-position"
          count={60}
          array={particlePositions}
          itemSize={3}
        />
      </bufferGeometry>
      <pointsMaterial 
        size={0.03} 
        color="#ffd700" 
        transparent 
        opacity={0.6}
        sizeAttenuation
      />
    </points>
  );
}

// Ascending arrows representing growth
function GrowthArrow({ position, delay }: { position: [number, number, number]; delay: number }) {
  const arrowRef = useRef<THREE.Group>(null);
  
  useFrame((state) => {
    if (arrowRef.current) {
      const t = (state.clock.elapsedTime + delay) % 4;
      arrowRef.current.position.y = position[1] + (t * 0.5) - 1;
      arrowRef.current.scale.setScalar(Math.sin(t * Math.PI / 4) * 0.8 + 0.2);
      const opacity = Math.sin(t * Math.PI / 4);
      arrowRef.current.children.forEach(child => {
        if (child instanceof THREE.Mesh && child.material instanceof THREE.MeshStandardMaterial) {
          child.material.opacity = opacity * 0.3;
        }
      });
    }
  });

  return (
    <group ref={arrowRef} position={position}>
      <Box args={[0.08, 0.25, 0.02]} position={[0, 0, 0]}>
        <meshStandardMaterial color="#ffc800" transparent opacity={0.3} />
      </Box>
      <Box args={[0.15, 0.08, 0.02]} position={[0, 0.16, 0]} rotation={[0, 0, Math.PI / 4]}>
        <meshStandardMaterial color="#ffc800" transparent opacity={0.3} />
      </Box>
      <Box args={[0.15, 0.08, 0.02]} position={[0, 0.16, 0]} rotation={[0, 0, -Math.PI / 4]}>
        <meshStandardMaterial color="#ffc800" transparent opacity={0.3} />
      </Box>
    </group>
  );
}

// Main 3D scene
function Scene() {
  return (
    <>
      {/* Ambient lighting */}
      <ambientLight intensity={0.4} />
      <pointLight position={[5, 5, 5]} intensity={0.8} color="#ffd700" />
      <pointLight position={[-5, -5, -5]} intensity={0.4} color="#ff9500" />
      
      {/* Central success orb */}
      <CentralOrb />
      
      {/* Career path rings */}
      <CareerRing radius={1.2} speed={0.3} yOffset={0} />
      <CareerRing radius={1.6} speed={-0.2} yOffset={0.1} />
      <CareerRing radius={2.0} speed={0.15} yOffset={-0.1} />
      
      {/* Floating resume documents */}
      <FloatingDocument position={[-1.8, 0.5, 0.5]} rotation={[0, 0.3, 0.1]} delay={0} />
      <FloatingDocument position={[1.9, -0.3, 0.3]} rotation={[0, -0.2, -0.1]} delay={1.5} />
      <FloatingDocument position={[0.5, 1.5, -0.5]} rotation={[0.1, 0.5, 0]} delay={3} />
      <FloatingDocument position={[-0.8, -1.4, 0.4]} rotation={[-0.1, -0.3, 0.05]} delay={2} />
      
      {/* Achievement particles */}
      <Particles />
      
      {/* Growth arrows */}
      <GrowthArrow position={[-1.3, 0, 0.8]} delay={0} />
      <GrowthArrow position={[1.4, 0, 0.6]} delay={1.3} />
      <GrowthArrow position={[0, 0, 1]} delay={2.6} />
    </>
  );
}

export function Hero3DScene() {
  return (
    <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
      <div className="w-full h-full max-w-4xl mx-auto">
        <Canvas
          camera={{ position: [0, 0, 5], fov: 45 }}
          style={{ background: 'transparent' }}
          gl={{ alpha: true, antialias: true }}
        >
          <Scene />
        </Canvas>
      </div>
    </div>
  );
}
